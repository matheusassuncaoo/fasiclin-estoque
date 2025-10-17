/**
 * InputValidationManager
 * Responsável por mascarar e validar inputs (datas, números, moeda)
 * - Datas não podem ser no passado
 * - Datas não podem estar muito no futuro (configurável)
 * - Quantidade somente inteiros >= 1
 * - Preço/Valor >= 0 com 2 casas decimais
 */
(function () {
  class InputValidationManager {
    constructor() {
      this.config = {
        // Limite máximo permitido para datas futuras (em dias)
        MAX_FUTURE_DAYS: 365,
      };

      // Elementos que iremos observar (definidos depois do DOM pronto)
      this.el = {
        dataPrev: null,
        dataOrdem: null,
        dataEntre: null,
        valorInicial: null,
        quantidadeInput: null,
        precoUnitarioInput: null,
      };
    }

    init() {
      // Mapear elementos se existirem
      this.el.dataPrev = document.getElementById("dataPrev");
      this.el.dataOrdem = document.getElementById("dataOrdem");
      this.el.dataEntre = document.getElementById("dataEntre");
      this.el.valorInicial = document.getElementById("valorInicial");
      this.el.quantidadeInput = document.getElementById("quantidadeInput");
      this.el.precoUnitarioInput =
        document.getElementById("precoUnitarioInput");

      this.setupDateConstraints();
      this.setupNumericConstraints();

      // Aplicar máscara de moeda pt-BR ao valor inicial (input type="text")
      if (this.el.valorInicial) {
        this.attachCurrencyMask(this.el.valorInicial);
      }
    }

    // =============================
    // Datas
    // =============================
    setupDateConstraints() {
      const today = this.toISODate(new Date());
      const maxFuture = this.toISODate(
        this.addDays(new Date(), this.config.MAX_FUTURE_DAYS)
      );

      // dataOrdem: hoje (já é readonly no HTML). Não invalidar se no passado quando editar registros antigos.
      if (this.el.dataOrdem) {
        this.el.dataOrdem.min = today;
        this.el.dataOrdem.max = maxFuture;
        // Se vazio, seta hoje
        if (!this.el.dataOrdem.value) this.el.dataOrdem.value = today;
        // Não aplicar setCustomValidity em dataOrdem por ser readonly e poder conter datas pretéritas legítimas
      }

      // dataPrev: pelo menos hoje, no máximo +MAX_FUTURE_DAYS
      if (this.el.dataPrev) {
        this.el.dataPrev.min = today;
        this.el.dataPrev.max = maxFuture;
        this.el.dataPrev.addEventListener("change", () =>
          this.validateDateField(this.el.dataPrev, {
            min: today,
            max: maxFuture,
            fieldName: "Data Prevista",
          })
        );
      }

      // dataEntre: não pode ser no passado; se existir dataOrdem, usar como min; se existir dataPrev, usar como max
      if (this.el.dataEntre) {
        const minEntre = this.el.dataOrdem?.value || today;
        const maxEntre = this.el.dataPrev?.value || maxFuture;
        this.el.dataEntre.min = minEntre;
        this.el.dataEntre.max = maxEntre;

        const recomputeBounds = () => {
          const min = this.el.dataOrdem?.value || today;
          const max = this.el.dataPrev?.value || maxFuture;
          this.el.dataEntre.min = min;
          this.el.dataEntre.max = max;
          this.validateDateField(this.el.dataEntre, {
            min,
            max,
            fieldName: "Data de Entrega",
          });
        };

        this.el.dataEntre.addEventListener("change", recomputeBounds);
        if (this.el.dataPrev)
          this.el.dataPrev.addEventListener("change", recomputeBounds);
        if (this.el.dataOrdem)
          this.el.dataOrdem.addEventListener("change", recomputeBounds);
      }
    }

    validateDateField(input, { min, max, fieldName }) {
      if (!input) return true;

      input.setCustomValidity("");
      input.classList.remove("is-invalid", "is-valid");

      const value = input.value;
      if (!value) return true;

      // Comparar como strings ISO yyyy-mm-dd é seguro
      if (min && value < min) {
        input.setCustomValidity(
          `${fieldName} não pode ser anterior a ${this.formatBR(min)}`
        );
      } else if (max && value > max) {
        input.setCustomValidity(
          `${fieldName} não pode ser posterior a ${this.formatBR(max)}`
        );
      }

      if (input.validationMessage) {
        input.classList.add("is-invalid");
        return false;
      } else {
        input.classList.add("is-valid");
        return true;
      }
    }

    // =============================
    // Números e Moeda
    // =============================
    setupNumericConstraints() {
      // Impedir caracteres inválidos em inputs numéricos (e, +, -)
      const blockInvalidChars = (e) => {
        if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
      };

      // Valor inicial agora é mascarado como moeda (tratado por attachCurrencyMask)

      // Quantidade (inteiro >= 1)
      if (this.el.quantidadeInput) {
        this.el.quantidadeInput.min = "1";
        this.el.quantidadeInput.step = "1";
        this.el.quantidadeInput.addEventListener("keypress", blockInvalidChars);
        this.el.quantidadeInput.addEventListener("input", () => {
          const v = parseInt(this.el.quantidadeInput.value || "0", 10);
          if (isNaN(v) || v < 1) {
            this.el.quantidadeInput.classList.add("is-invalid");
          } else {
            this.el.quantidadeInput.classList.remove("is-invalid");
          }
        });
        this.el.quantidadeInput.addEventListener("blur", () => {
          const v = parseInt(this.el.quantidadeInput.value || "0", 10);
          if (!isNaN(v) && v >= 1) this.el.quantidadeInput.value = String(v);
        });
      }

      // Preço Unitário (>= 0, 2 casas)
      if (this.el.precoUnitarioInput) {
        this.el.precoUnitarioInput.min = "0";
        this.el.precoUnitarioInput.step = "0.01";
        this.el.precoUnitarioInput.addEventListener(
          "keypress",
          blockInvalidChars
        );
        this.el.precoUnitarioInput.addEventListener("input", () => {
          const v = parseFloat(this.el.precoUnitarioInput.value);
          if (isNaN(v) || v < 0) {
            this.el.precoUnitarioInput.classList.add("is-invalid");
          } else {
            this.el.precoUnitarioInput.classList.remove("is-invalid");
          }
        });
        this.el.precoUnitarioInput.addEventListener("blur", () => {
          const v = parseFloat(this.el.precoUnitarioInput.value);
          if (!isNaN(v)) this.el.precoUnitarioInput.value = v.toFixed(2);
        });
      }
    }

    // =============================
    // Máscara de Moeda pt-BR
    // =============================
    attachCurrencyMask(input) {
      if (!input) return;

      const formatBRL = (num) => {
        try {
          console.log("[DEBUG] formatBRL input num:", num);
          const formatted = new Intl.NumberFormat("pt-BR", {
            style: "currency",
            currency: "BRL",
          }).format(num);
          console.log("[DEBUG] formatBRL result:", formatted);
          return formatted;
        } catch (_) {
          const fallback = "R$ " + (Number(num) || 0).toFixed(2);
          console.log("[DEBUG] formatBRL fallback:", fallback);
          return fallback;
        }
      };

      const parseToNumber = (value) => {
        if (!value) return 0;
        console.log("[DEBUG] parseToNumber input:", value);
        
        // Versão simplificada com logs para debug
        const only = String(value).replace(/[^0-9,]/g, "");
        console.log("[DEBUG] parseToNumber only digits and comma:", only);
        if (!only) return 0;
        
        const normalized = only.replace(/\./g, "").replace(",", ".");
        console.log("[DEBUG] parseToNumber normalized:", normalized);
        
        const n = parseFloat(normalized);
        const result = isNaN(n) ? 0 : n;
        console.log("[DEBUG] parseToNumber final result:", result);
        
        return result;
      };

      const onInput = (e) => {
        const caretFromEnd = input.value.length - (input.selectionStart || 0);
        const num = parseToNumber(e.target.value);
        e.target.value = num === 0 ? "" : formatBRL(num);
        const newPos = Math.max(0, e.target.value.length - caretFromEnd);
        requestAnimationFrame(() => {
          try {
            input.setSelectionRange(newPos, newPos);
          } catch {}
        });
      };

      const onFocus = (e) => {
        // Seleciona tudo para facilitar edição
        e.target.select();
      };

      const onBlur = (e) => {
        const num = parseToNumber(e.target.value);
        e.target.value = num ? formatBRL(num) : "";
      };

      input.addEventListener("input", onInput);
      input.addEventListener("focus", onFocus);
      input.addEventListener("blur", onBlur);

      // Se já houver um valor numérico simples, formatar
      if (input.value && !/^R\$/.test(input.value)) {
        const num = parseToNumber(input.value);
        input.value = num ? formatBRL(num) : "";
      }

      // Guardar parser para consumo externo
      input.__parseCurrencyToNumber = parseToNumber;
    }

    getNumericValueFromCurrency(input) {
      if (!input) return 0;
      console.log("[DEBUG] getNumericValueFromCurrency input.value:", input.value);
      if (typeof input.__parseCurrencyToNumber === "function") {
        const result = input.__parseCurrencyToNumber(input.value);
        console.log("[DEBUG] getNumericValueFromCurrency __parseCurrencyToNumber result:", result);
        return result;
      }
      const v = String(input.value || "")
        .replace(/[^0-9,]/g, "")
        .replace(",", ".");
      console.log("[DEBUG] getNumericValueFromCurrency fallback v:", v);
      const n = parseFloat(v);
      console.log("[DEBUG] getNumericValueFromCurrency fallback result:", n);
      return isNaN(n) ? 0 : n;
    }

    // =============================
    // Helpers
    // =============================
    addDays(date, days) {
      const d = new Date(date);
      d.setDate(d.getDate() + days);
      return d;
    }

    toISODate(date) {
      return new Date(date).toISOString().split("T")[0];
    }

    formatBR(isoDate) {
      const [y, m, d] = isoDate.split("-");
      return `${d}/${m}/${y}`;
    }
  }

  // Singleton global
  const validator = new InputValidationManager();
  window.inputValidator = validator;

  // Inicializa assim que o DOM estiver pronto
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => validator.init());
  } else {
    validator.init();
  }
})();

console.log("[InputValidationManager] Inicializado");
