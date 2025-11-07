// O Endereço base da sua API Java (pode ser necessário mudar a porta se não for 8080)
const API_URL = 'http://localhost:8080/api';

// Estado do Carrinho
let carrinho = [];

// Função utilitária para formatar valores monetários
const formatarMoeda = (valor) => valor.toFixed(2).replace('.', ',');

// 1. Carregar Cardápio ao iniciar
document.addEventListener('DOMContentLoaded', carregarCardapio);

async function carregarCardapio() {
    const listaItensDiv = document.getElementById('lista-itens');
    
    // Tenta obter os itens do seu Backend Java
    try {
        // Chamada para a rota que lista os Itens (Lanches, Bebidas, Combos)
        const response = await fetch(`${API_URL}/cardapio`); 
        
        if (!response.ok) {
            throw new Error(`Erro HTTP! Status: ${response.status}`);
        }
        
        const cardapio = await response.json();

        if (cardapio.length === 0) {
            listaItensDiv.innerHTML = '<p>Nenhum item encontrado no cardápio (Verifique o Backend Java).</p>';
            return;
        }

        listaItensDiv.innerHTML = ''; // Limpa o "Carregando..."
        
        cardapio.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'item-card';
            // Assumimos que o objeto Item do Java tem: id, nome, preco, estoque.
            itemElement.innerHTML = `
                <h3>${item.nome}</h3>
                <p>Preço: R$ ${formatarMoeda(item.preco)}</p>
                <p>Estoque: ${item.estoque > 0 ? item.estoque : 'ESGOTADO'}</p>
                <button onclick="adicionarAoCarrinho(${item.id}, '${item.nome}', ${item.preco})" 
                        ${item.estoque <= 0 ? 'disabled' : ''}>
                    Adicionar
                </button>
            `;
            listaItensDiv.appendChild(itemElement);
        });

    } catch (error) {
        console.error('Erro ao carregar cardápio:', error);
        listaItensDiv.innerHTML = `
            <p style="color: red;">🚨 Não foi possível conectar ao Backend Java (API_URL: ${API_URL}/cardapio).</p>
            <p>Verifique se o seu servidor Spring Boot (IntelliJ) está rodando na porta 8080.</p>
        `;
    }
}

// 2. Adicionar Item ao Carrinho (Frontend)
function adicionarAoCarrinho(id, nome, preco) {
    const itemExistente = carrinho.find(item => item.id === id);

    if (itemExistente) {
        itemExistente.quantidade++;
    } else {
        carrinho.push({ id, nome, preco, quantidade: 1 });
    }

    atualizarCarrinhoDOM();
}

// 3. Atualizar a visualização do Carrinho e o Total
function atualizarCarrinhoDOM() {
    const itensCarrinhoDiv = document.getElementById('itens-carrinho');
    const valorTotalSpan = document.getElementById('valor-total');
    let total = 0;

    itensCarrinhoDiv.innerHTML = ''; 

    if (carrinho.length === 0) {
        itensCarrinhoDiv.innerHTML = '<p>Nenhum item no carrinho.</p>';
        valorTotalSpan.textContent = formatarMoeda(0);
        return;
    }

    carrinho.forEach(item => {
        const subtotal = item.preco * item.quantidade;
        total += subtotal;

        const itemElement = document.createElement('div');
        itemElement.className = 'item-carrinho';
        itemElement.innerHTML = `
            <span>${item.nome} (x${item.quantidade})</span>
            <span>R$ ${formatarMoeda(subtotal)}</span>
        `;
        itensCarrinhoDiv.appendChild(itemElement);
    });

    valorTotalSpan.textContent = formatarMoeda(total);
}

// 4. Finalizar Venda (Comunicação com o Backend)
async function finalizarVenda() {
    if (carrinho.length === 0) {
        alert('Seu carrinho está vazio!');
        return;
    }

    const clienteIdInput = document.getElementById('cliente-id').value.trim();
    const clienteId = clienteIdInput === '' ? null : clienteIdInput; // Envia null se vazio para usar o Cliente Padrão

    const itensVenda = carrinho.map(item => ({
        itemId: item.id,
        quantidade: item.quantidade
    }));

    const dadosVenda = {
        clienteId: clienteId,
        itens: itensVenda 
        // Assumimos que o total será calculado no Backend (melhor Prática de Confiabilidade)
    };
    
    // Alerta de confirmação simples
    if (!confirm(`Deseja finalizar a compra no valor de R$ ${document.getElementById('valor-total').textContent}?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/vendas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dadosVenda)
        });

        const resultado = await response.json();
        
        if (response.ok) {
            // Sucesso na Venda
            alert(`✅ Pedido realizado com sucesso! Total: R$ ${formatarMoeda(resultado.total)}. ID da Venda: ${resultado.id}`);
            
            // Limpa o estado
            carrinho = [];
            atualizarCarrinhoDOM();
            carregarCardapio(); // Recarrega para refletir a redução do estoque
            document.getElementById('cliente-id').value = '';
        } else {
            // Erro na lógica de negócio (ex: estoque insuficiente, cliente não encontrado)
            alert(`❌ Erro ao finalizar o pedido: ${resultado.message || 'Verifique o console do navegador e o log do servidor Java.'}`);
            console.error('Resposta de erro do servidor:', resultado);
        }

    } catch (error) {
        // Erro de rede/conexão
        alert('❌ Erro de comunicação com o servidor. O Backend Java está ativo?');
        console.error('Erro de rede ou na requisição:', error);
    }
}