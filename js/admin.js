// O Endereço base da sua API Java
const API_URL = 'http://localhost:8080/api';
// ID do Cliente Padrão (como definido no Backend)
const CLIENTE_PADRAO_ID = 0; 

// Função utilitária para formatar valores monetários
const formatarMoeda = (valor) => parseFloat(valor).toFixed(2).replace('.', ',');

// Executa as funções iniciais ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    carregarItensZerados();
    carregarClientesMaisGastaram();
});

// --- CADASTRO DE CLIENTE ---

async function cadastrarNovoCliente() {
    const nome = document.getElementById('nomeCliente').value.trim();
    const msgDiv = document.getElementById('msg-cliente');

    if (!nome) {
        msgDiv.textContent = 'Por favor, digite o nome do cliente.';
        msgDiv.style.color = 'orange';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/clientes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome: nome })
        });

        const resultado = await response.json();

        if (response.ok && resultado.id) {
            msgDiv.innerHTML = `✅ Cliente <b>${resultado.nome}</b> cadastrado com sucesso! ID: <b>${resultado.id}</b>`;
            msgDiv.style.color = 'green';
            document.getElementById('nomeCliente').value = ''; 
        } else {
            msgDiv.textContent = `❌ Falha no cadastro: ${resultado.message || 'Erro desconhecido.'}`;
            msgDiv.style.color = 'red';
        }

    } catch (error) {
        msgDiv.textContent = 'Erro de comunicação com o servidor Java.';
        msgDiv.style.color = 'red';
        console.error(error);
    }
}


// --- ENTRADA DE ESTOQUE ---

async function entradaEstoque() {
    const itemId = document.getElementById('itemId').value;
    const quantidade = document.getElementById('quantidadeEntrada').value;
    const msgDiv = document.getElementById('msg-estoque');

    if (!itemId || !quantidade || parseInt(quantidade) <= 0) {
        msgDiv.textContent = 'Preencha um ID de Item e uma quantidade válida.';
        msgDiv.style.color = 'orange';
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/estoque/entrada`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ itemId: parseInt(itemId), quantidade: parseInt(quantidade) })
        });

        if (response.ok) {
            msgDiv.textContent = `✅ Estoque do item ${itemId} aumentado em ${quantidade}!`;
            msgDiv.style.color = 'green';
            carregarItensZerados(); // Atualiza a lista de estoque
        } else {
            const errorData = await response.json();
            msgDiv.textContent = `❌ Falha na entrada de estoque: ${errorData.message || 'Erro desconhecido.'}`;
            msgDiv.style.color = 'red';
        }

    } catch (error) {
        msgDiv.textContent = '❌ Erro de comunicação com o servidor Java.';
        msgDiv.style.color = 'red';
        console.error(error);
    }
}


// --- CONSULTA 1: ITENS COM ESTOQUE ZERADO ---

async function carregarItensZerados() {
    const tbody = document.getElementById('tabela-estoque-zerado').querySelector('tbody');
    tbody.innerHTML = '<tr><td colspan="4">Carregando...</td></tr>';

    try {
        const response = await fetch(`${API_URL}/consultas/estoque-zerado`); 
        const itens = await response.json();

        tbody.innerHTML = '';
        if (itens.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">✅ Nenhum item com estoque zerado!</td></tr>';
            return;
        }

        itens.forEach(item => {
            const row = tbody.insertRow();
            row.insertCell().textContent = item.id;
            row.insertCell().textContent = item.nome;
            row.insertCell().textContent = item.tipo;
            row.insertCell().textContent = item.estoque; 
        });

    } catch (error) {
        console.error('Erro ao carregar itens zerados:', error);
        tbody.innerHTML = '<tr><td colspan="4" style="color: red;">❌ Erro ao conectar ao Backend.</td></tr>';
    }
}


// --- CONSULTA 2: CLIENTES QUE MAIS GASTARAM ---

async function carregarClientesMaisGastaram() {
    const tbody = document.getElementById('tabela-clientes-top').querySelector('tbody');
    tbody.innerHTML = '<tr><td colspan="3">Carregando...</td></tr>';

    try {
        const response = await fetch(`${API_URL}/consultas/clientes-top`);
        
        if (!response.ok) {
            throw new Error('Erro na API');
        }
        
        const clientes = await response.json();

        tbody.innerHTML = '';
        if (clientes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3">Nenhuma venda registrada ainda.</td></tr>';
            return;
        }

        clientes.forEach((cliente, index) => {
            const row = tbody.insertRow();
            row.insertCell().textContent = index + 1; // Posição
            row.insertCell().textContent = cliente.id;
            row.insertCell().textContent = cliente.nome;
        });

    } catch (error) {
        console.error('Erro ao carregar clientes top:', error);
        tbody.innerHTML = '<tr><td colspan="3" style="color: red;">❌ Erro ao conectar com o Backend.</td></tr>';
    }
}


// --- CONSULTA 3: VENDAS POR CLIENTE (CÓDIGO AJUSTADO) ---

async function carregarVendasPorCliente() {
    const clienteId = document.getElementById('input-cliente-id').value;
    const tbody = document.getElementById('tabela-vendas-cliente').querySelector('tbody');
    tbody.innerHTML = '<tr><td colspan="3">Buscando...</td></tr>';

    if (!clienteId) {
        tbody.innerHTML = '<tr><td colspan="3" style="color: orange;">Por favor, informe o ID do cliente.</td></tr>';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/consultas/vendas-por-cliente?clienteId=${clienteId}`);
        const resultado = await response.json();

        tbody.innerHTML = '';

        if (!response.ok) {
            tbody.innerHTML = `<tr><td colspan="3" style="color: red;">❌ Erro: ${resultado.message || 'Cliente não encontrado.'}</td></tr>`;
            return;
        }

        if (resultado.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3">Nenhuma venda encontrada para o Cliente ID ${clienteId}.</td></tr>`;
            return;
        }

        resultado.forEach(venda => {
            const row = tbody.insertRow();
            row.insertCell().textContent = venda.id;
            // Ajuste visual para o total (Negrito/Cor de destaque - #B30000)
            row.insertCell().innerHTML = `<span style="font-weight: bold; color: #B30000;">R$ ${formatarMoeda(venda.total)}</span>`;
            // Ajusta o formato da data/hora (Legibilidade)
            row.insertCell().textContent = new Date(venda.dataVenda).toLocaleString('pt-BR');
        });

    } catch (error) {
        console.error('Erro ao buscar vendas por cliente:', error);
        tbody.innerHTML = '<tr><td colspan="3" style="color: red;">❌ Erro de comunicação com o Backend.</td></tr>';
    }
}


// --- CONSULTA 4: VENDAS POR DIA (CÓDIGO AJUSTADO) ---

async function carregarVendasPorDia() {
    const data = document.getElementById('input-data').value;
    const tbody = document.getElementById('tabela-vendas-dia').querySelector('tbody');
    tbody.innerHTML = '<tr><td colspan="3">Buscando...</td></tr>';

    if (!data) {
        tbody.innerHTML = '<tr><td colspan="3" style="color: orange;">Por favor, informe a data no formato YYYY-MM-DD.</td></tr>';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/consultas/vendas-por-dia?data=${data}`);
        const vendas = await response.json();

        tbody.innerHTML = '';
        
        if (vendas.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3">Nenhuma venda encontrada na data ${data}.</td></tr>`;
            return;
        }

        vendas.forEach(venda => {
            const row = tbody.insertRow();
            row.insertCell().textContent = venda.id;
            // Mostra o nome do cliente ou "Padrão"
            row.insertCell().textContent = (venda.cliente && venda.cliente.nome) || 'Padrão'; 
            // Ajuste visual para o total (Negrito/Cor de destaque)
            row.insertCell().innerHTML = `<span style="font-weight: bold; color: #B30000;">R$ ${formatarMoeda(venda.total)}</span>`;
        });

    } catch (error) {
        console.error('Erro ao buscar vendas por dia:', error);
        tbody.innerHTML = '<tr><td colspan="3" style="color: red;">❌ Erro de comunicação com o Backend.</td></tr>';
    }
}