package com.lanchonete.sistema_lanchonete.controller;

import com.lanchonete.sistema_lanchonete.model.Cliente;
import com.lanchonete.sistema_lanchonete.model.Item;
import com.lanchonete.sistema_lanchonete.model.Venda;
import com.lanchonete.sistema_lanchonete.service.LanchoneteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@CrossOrigin(origins = "*") // Permite acesso de qualquer origem (crucial para o VSCode se comunicar)
@RestController // Define a classe como um controlador REST
@RequestMapping("/api") // Prefixo de todas as rotas
public class LanchoneteController {

    private final LanchoneteService service;

    public LanchoneteController(LanchoneteService service) {
        this.service = service;
    }

    // Rota GET /api/cardapio
    @GetMapping("/cardapio")
    public List<Item> getCardapio() {
        return service.listarCardapio();
    }

    // Rota POST /api/vendas
    @PostMapping("/vendas")
    public ResponseEntity<?> realizarVenda(@RequestBody LanchoneteService.VendaDTO dto) {
        try {
            Venda venda = service.realizarVenda(dto);
            // Corretude e Padrão: Retorna 201 Created com os dados da venda
            return new ResponseEntity<>(venda, HttpStatus.CREATED);
        } catch (Exception e) {
            // Confiabilidade: Retorna 400 Bad Request em caso de erro de negócio (ex: estoque)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ------------------------------------
    // ROTAS DA ÁREA ADMINISTRATIVA / CONSULTAS
    // ------------------------------------

    // 1. Listagem de Itens com estoque zerado.
    @GetMapping("/consultas/estoque-zerado")
    public List<Item> getItensZerados() {
        return service.listarItensZerados();
    }

    // 2. Listagem de Clientes que mais gastaram.
    @GetMapping("/consultas/clientes-top")
    public List<Cliente> getClientesTop() {
        return service.listarClientesQueMaisGastaram();
    }

    // 3. Listagem de Vendas por Cliente.
    // Rota GET /api/consultas/vendas-por-cliente?clienteId={id}
    @GetMapping("/consultas/vendas-por-cliente")
    public ResponseEntity<?> getVendasPorCliente(@RequestParam Long clienteId) {
        try {
            List<Venda> vendas = service.listarVendasPorCliente(clienteId);
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404 se o cliente não for encontrado
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 4. Listagem de Vendas por dia.
    // Rota GET /api/consultas/vendas-por-dia?data={YYYY-MM-DD}
    @GetMapping("/consultas/vendas-por-dia")
    public List<Venda> getVendasPorDia(@RequestParam String data) {
        return service.listarVendasPorDia(data);
    }

    // Rota de Entrada de Estoque (Funcionalidade de Controle)

    // DTO simples para receber dados de entrada de estoque do frontend
    public static class EstoqueEntradaDTO {
        public Long itemId;
        public Integer quantidade;
    }

    @PostMapping("/estoque/entrada")
    public ResponseEntity<Map<String, String>> entradaEstoque(@RequestBody EstoqueEntradaDTO dto) {
        try {
            // Corretude: Chama o serviço para adicionar o estoque
            service.adicionarEstoque(dto.itemId, dto.quantidade);
            return ResponseEntity.ok(Map.of("message", "Estoque atualizado com sucesso."));
        } catch (Exception e) {
            // Confiabilidade: Retorna erro se o item não for encontrado
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ROTA ADICIONADA: Cadastro de Clientes

    // DTO simples para receber o nome do cliente
    public static class ClienteCadastroDTO {
        public String nome;
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> cadastrarCliente(@RequestBody ClienteCadastroDTO dto) {
        try {
            // Confiabilidade: Validação simples para garantir que o nome não é vazio
            if (dto.nome == null || dto.nome.trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "O nome do cliente não pode ser vazio."));
            }

            Cliente novoCliente = service.cadastrarCliente(dto.nome);
            // Retorna 201 Created com os dados do novo cliente
            return new ResponseEntity<>(novoCliente, HttpStatus.CREATED);
        } catch (Exception e) {
            // Em caso de erro interno
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao cadastrar cliente: " + e.getMessage()));
        }
    }
}