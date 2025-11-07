package com.lanchonete.sistema_lanchonete.model;

import java.time.LocalDateTime;
import java.util.Map; // Para guardar {itemId: quantidade}

public class Venda {
    private Long id;
    private Cliente cliente;
    private Map<Long, Integer> itensVendidos; // Map<IdItem, Quantidade>
    private double total;
    private LocalDateTime dataVenda;

    public Venda(Long id, Cliente cliente, Map<Long, Integer> itensVendidos, double total) {
        this.id = id;
        this.cliente = cliente;
        this.itensVendidos = itensVendidos;
        this.total = total;
        this.dataVenda = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Map<Long, Integer> getItensVendidos() {
        return itensVendidos;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }
}