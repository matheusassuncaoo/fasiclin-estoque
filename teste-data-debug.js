// Teste temporário para debug de datas
// Cole este código no console do navegador e execute

async function testeDataDirecto() {
    console.log("🧪 TESTE DIRETO DE ATUALIZAÇÃO DE DATA");
    
    try {
        // 1. Pegar primeira ordem
        const ordens = await apiManager.getOrdensCompra();
        const ordem = ordens[0] || ordens.content[0];
        
        console.log("📋 Ordem original:", ordem);
        
        // 2. Criar payload de teste com data específica
        const novaDataEntre = "2025-12-25"; // Natal!
        
        const payload = {
            id: ordem.id,
            statusOrdemCompra: ordem.statusOrdemCompra,
            valor: ordem.valor,
            dataPrev: ordem.dataPrev,
            dataOrdem: ordem.dataOrdem,
            dataEntre: novaDataEntre
        };
        
        console.log("📤 Payload sendo enviado:", payload);
        
        // 3. Fazer chamada direta
        const response = await fetch(`http://localhost:8080/api/ordens-compra/${ordem.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        const resultado = await response.json();
        console.log("📥 Resposta do backend:", resultado);
        
        // 4. Verificar se a data foi salva corretamente
        const ordemVerificacao = await apiManager.getOrdemCompra(ordem.id);
        console.log("🔍 Ordem após verificação:", ordemVerificacao);
        
        if (ordemVerificacao.dataEntre === novaDataEntre) {
            console.log("✅ DATA SALVA CORRETAMENTE!");
        } else {
            console.log("❌ DATA FOI ALTERADA!");
            console.log("Enviado:", novaDataEntre);
            console.log("Recebido:", ordemVerificacao.dataEntre);
        }
        
    } catch (error) {
        console.error("❌ Erro no teste:", error);
    }
}

// Executar o teste
testeDataDirecto();