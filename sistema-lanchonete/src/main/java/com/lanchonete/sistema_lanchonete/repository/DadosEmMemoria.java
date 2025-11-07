package com.lanchonete.sistema_lanchonete.repository;

import com.lanchonete.sistema_lanchonete.model.Cliente;
import com.lanchonete.sistema_lanchonete.model.Item;
import com.lanchonete.sistema_lanchonete.model.Venda;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component // Indica que é um componente Spring
public class DadosEmMemoria {

    // Simulação do nosso banco de dados
    private final List<Item> itens = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Venda> vendas = new ArrayList<>();

    // Contadores para gerar IDs (garante Reusabilidade e Corretude)
    private final AtomicLong itemIdCounter = new AtomicLong(0);
    private final AtomicLong clienteIdCounter = new AtomicLong(0); // Inicia em 0
    private final AtomicLong vendaIdCounter = new AtomicLong(0);

    // Cliente padrão para quando não for informado
    // O ID 0 será usado para o Cliente Padrão
    public static final Cliente CLIENTE_PADRAO = new Cliente(0L, "Cliente Padrão");

    public DadosEmMemoria() {

        // --- Clientes ---

        // 1. Adiciona o Cliente Padrão (ID 0) manualmente
        clientes.add(CLIENTE_PADRAO);

        // 2. Adiciona os clientes iniciais e incrementa o contador
        clientes.add(new Cliente(clienteIdCounter.incrementAndGet(), "Alice Silva")); // ID 1
        clientes.add(new Cliente(clienteIdCounter.incrementAndGet(), "Bruno Souza")); // ID 2

        // --- Cardápio e Estoque ---
        itens.add(new Item(itemIdCounter.incrementAndGet(), "X-Salada", 18.00, 20, "Lanche"));
        itens.add(new Item(itemIdCounter.incrementAndGet(), "Coca-Cola Lata", 6.00, 30, "Bebida"));
        itens.add(new Item(itemIdCounter.incrementAndGet(), "Combo 1 (Lanche + Refri)", 22.00, 15, "Combo"));
        itens.add(new Item(itemIdCounter.incrementAndGet(), "Água Mineral", 4.00, 0, "Bebida"));
    }

    // Métodos de acesso para as listas
    public List<Item> getItens() { return itens; }
    public List<Cliente> getClientes() { return clientes; }
    public List<Venda> getVendas() { return vendas; }

    // Métodos para gerar novos IDs
    public Long getNextItemId() { return itemIdCounter.incrementAndGet(); }
    public Long getNextClienteId() {

        return clienteIdCounter.incrementAndGet();
    }
    public Long getNextVendaId() { return vendaIdCounter.incrementAndGet(); }
}