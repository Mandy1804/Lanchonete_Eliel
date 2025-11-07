package com.lanchonete.sistema_lanchonete.model;

public class Item {
    private Long id;
    private String nome;
    private double preco;
    private int estoque; // Controle de estoque
    private String tipo; // Lanche, Bebida, Combo

    // Construtor completo
    public Item(Long id, String nome, double preco, int estoque, String tipo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.tipo = tipo;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public String getTipo() {
        return tipo;
    }


    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }
}