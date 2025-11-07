package com.lanchonete.sistema_lanchonete.service;

import com.lanchonete.sistema_lanchonete.model.Cliente;
import com.lanchonete.sistema_lanchonete.model.Item;
import com.lanchonete.sistema_lanchonete.model.Venda;
import com.lanchonete.sistema_lanchonete.repository.DadosEmMemoria;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LanchoneteService {

    private final DadosEmMemoria dados; // Injeção de dependência

    public LanchoneteService(DadosEmMemoria dados) {
        this.dados = dados;
    }

    // --- Métodos de Listagem Simples ---

    public List<Item> listarCardapio() {
        return dados.getItens();
    }

    // --- Cadastro e Estoque ---

    // Adequação aos padrões / Corretude: Permite entradas no estoque
    public void adicionarEstoque(Long itemId, int quantidade) throws Exception {
        Optional<Item> itemOpt = dados.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();

        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            item.setEstoque(item.getEstoque() + quantidade);
        } else {
            throw new Exception("Item não encontrado."); // Confiabilidade: trata item inexistente
        }
    }

    public Cliente cadastrarCliente(String nome) {
        Long id = dados.getNextClienteId();
        Cliente novoCliente = new Cliente(id, nome);
        dados.getClientes().add(novoCliente);
        return novoCliente;
    }

    // --- Lógica de Venda (Núcleo da Confiabilidade e Corretude) ---

    // A VendaDTO é uma classe interna simples para receber os dados do Frontend
    public static class VendaDTO {
        public Long clienteId;
        public List<Map<String, Object>> itens; // Recebe [{itemId: 1, quantidade: 2}, ...]
    }

    public Venda realizarVenda(VendaDTO dto) throws Exception {

        // 1. Encontrar Cliente (Corretude: Cliente ou Cliente Padrão)
        Cliente cliente;
        if (dto.clienteId == null || dto.clienteId.equals(0L)) {
            // Se for nulo ou 0, usa o cliente padrão (ID 0)
            cliente = DadosEmMemoria.CLIENTE_PADRAO;
        } else {
            // Busca o cliente pelo ID
            cliente = dados.getClientes().stream()
                    .filter(c -> c.getId().equals(dto.clienteId))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Cliente não encontrado.")); // Confiabilidade
        }

        Map<Long, Integer> itensVendidos = dto.itens.stream()
                .collect(Collectors.toMap(
                        itemMap -> Long.valueOf(itemMap.get("itemId").toString()),
                        itemMap -> Integer.valueOf(itemMap.get("quantidade").toString())
                ));

        double totalVenda = 0;

        // 2. Pré-verificação de Estoque e Cálculo do Total (Confiabilidade)
        for (Map.Entry<Long, Integer> entry : itensVendidos.entrySet()) {
            Long itemId = entry.getKey();
            int quantidade = entry.getValue();

            Item item = dados.getItens().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Item do pedido não encontrado.")); // Confiabilidade

            if (item.getEstoque() < quantidade) {
                // Confiabilidade: Falha e informa qual item não tem estoque
                throw new Exception("Estoque insuficiente para " + item.getNome() + ". Disponível: " + item.getEstoque());
            }
            totalVenda += item.getPreco() * quantidade;
        }

        // 3. Efetivar a Venda e Dedução de Estoque
        for (Map.Entry<Long, Integer> entry : itensVendidos.entrySet()) {
            Item item = dados.getItens().stream()
                    .filter(i -> i.getId().equals(entry.getKey()))
                    .findFirst().get();

            // Controle de estoque (Corretude)
            item.setEstoque(item.getEstoque() - entry.getValue());
        }

        // 4. Salvar a Venda (Corretude)
        // Usa o objeto 'cliente' encontrado/selecionado no passo 1
        Venda novaVenda = new Venda(dados.getNextVendaId(), cliente, itensVendidos, totalVenda);
        dados.getVendas().add(novaVenda);

        return novaVenda;
    }

    // --- Métodos de Consultas (Corretude) ---

    // 1. Listagem de Itens com estoque zerado.
    public List<Item> listarItensZerados() {
        return dados.getItens().stream()
                .filter(item -> item.getEstoque() <= 0)
                .collect(Collectors.toList());
    }

    // 2. Listagem de Clientes que mais gastaram. (CORRIGIDO PARA USAR O ID DO CLIENTE)
    public List<Cliente> listarClientesQueMaisGastaram() {

        // Passo 1: Agrupa Vendas pelo ID do Cliente e soma os gastos por ID.
        Map<Long, Double> gastosPorId = dados.getVendas().stream()
                .collect(Collectors.groupingBy(
                        venda -> venda.getCliente().getId(), // CORREÇÃO: Agrupa pelo ID (chave única)
                        Collectors.summingDouble(Venda::getTotal)
                ));

        // Passo 2: Mapeia o resultado (ID, Gasto) para o objeto Cliente e ordena.
        return gastosPorId.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Ordena pelo gasto
                .limit(10) // Limita aos 10 que mais gastaram
                .map(entry -> dados.getClientes().stream()
                        .filter(c -> c.getId().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null)) // Encontra o objeto Cliente correspondente
                .filter(cliente -> cliente != null)
                .collect(Collectors.toList());
    }

    // 3. Listagem de Vendas por Cliente (Implementação simples)
    public List<Venda> listarVendasPorCliente(Long clienteId) throws Exception {
        // Encontrar o cliente para garantir que ele existe (Confiabilidade)
        Optional<Cliente> clienteOpt = dados.getClientes().stream()
                .filter(c -> c.getId().equals(clienteId))
                .findFirst();

        if (clienteOpt.isEmpty()) {
            throw new Exception("Cliente não encontrado.");
        }

        return dados.getVendas().stream()
                .filter(v -> v.getCliente().getId().equals(clienteId))
                .collect(Collectors.toList());
    }

    // 4. Listagem de Vendas por dia (Implementação simples)
    public List<Venda> listarVendasPorDia(String data) {
        // Assumindo data no formato YYYY-MM-DD
        return dados.getVendas().stream()
                .filter(v -> v.getDataVenda().toLocalDate().toString().equals(data))
                .collect(Collectors.toList());
    }
}