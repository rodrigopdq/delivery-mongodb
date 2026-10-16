package br.pucgoias.ads.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import br.pucgoias.ads.delivery.dominio.Cliente;
import br.pucgoias.ads.delivery.dominio.Endereco;
import br.pucgoias.ads.delivery.dominio.FaturamentoRestaurante;
import br.pucgoias.ads.delivery.dominio.ItemCardapio;
import br.pucgoias.ads.delivery.dominio.ItemPedido;
import br.pucgoias.ads.delivery.dominio.Pedido;
import br.pucgoias.ads.delivery.dominio.Restaurante;
import br.pucgoias.ads.delivery.dominio.StatusPedido;
import br.pucgoias.ads.delivery.excecao.ItemDuplicadoException;
import br.pucgoias.ads.delivery.excecao.ItemIndisponivelException;
import br.pucgoias.ads.delivery.excecao.PedidoInvalidoException;
import br.pucgoias.ads.delivery.excecao.RestauranteDuplicadoException;
import br.pucgoias.ads.delivery.excecao.TransicaoInvalidaException;
import br.pucgoias.ads.delivery.repositorio.PedidoRepository;
import br.pucgoias.ads.delivery.repositorio.RestauranteRepository;
import br.pucgoias.ads.delivery.servico.DeliveryService;
import br.pucgoias.ads.delivery.servico.ItemSolicitado;

/**
 * Casos de teste do laboratorio. Requer MongoDB em execucao em localhost:27017
 * (banco delivery_test). A limpeza usa deleteAll() para preservar os indices.
 */
@DataMongoTest
@Import(DeliveryService.class)
class DeliveryServiceTest {

    @Autowired DeliveryService servico;
    @Autowired RestauranteRepository restauranteRepository;
    @Autowired PedidoRepository pedidoRepository;

    private Restaurante cerrado;
    private final Cliente ana = new Cliente("Ana Souza", "62999990001");

    @BeforeEach
    void preparar() {
        pedidoRepository.deleteAll();
        restauranteRepository.deleteAll();
        Restaurante r = new Restaurante("Sabor do Cerrado", "Goiana",
                new Endereco("Rua T-27, 100", "Setor Bueno", "Goiania"));
        r.incluirNoCardapio(new ItemCardapio("PAMONHA", "Pamonha de sal", new BigDecimal("12.00"), true));
        r.incluirNoCardapio(new ItemCardapio("EMPADAO", "Empadao goiano", new BigDecimal("25.50"), true));
        r.incluirNoCardapio(new ItemCardapio("PEQUI", "Arroz com pequi", new BigDecimal("32.00"), false));
        cerrado = servico.cadastrarRestaurante(r);
    }

    private Pedido pedidoEntregue(String restauranteId, String codigo, int quantidade) {
        Pedido p = servico.criarPedido(restauranteId, ana, List.of(new ItemSolicitado(codigo, quantidade)));
        servico.avancarStatus(p.getId());
        servico.avancarStatus(p.getId());
        return servico.avancarStatus(p.getId());
    }

    // ---------------- Casos fornecidos ----------------

    @Test
    @DisplayName("1. Cadastro gera identificador e persiste o cardapio incorporado")
    void caso1_cadastro() {
        Restaurante lido = servico.buscarRestaurante(cerrado.getId());
        assertThat(lido.getId()).isNotBlank();
        assertThat(lido.getCardapio()).hasSize(3);
        assertThat(lido.buscarItem("EMPADAO").orElseThrow().preco()).isEqualByComparingTo("25.50");
    }

    @Test
    @DisplayName("2. Nome de restaurante duplicado e rejeitado (R1)")
    void caso2_nomeDuplicado() {
        Restaurante copia = new Restaurante("Sabor do Cerrado", "Goiana",
                new Endereco("Av. 85, 50", "Setor Marista", "Goiania"));
        assertThatThrownBy(() -> servico.cadastrarRestaurante(copia))
                .isInstanceOf(RestauranteDuplicadoException.class)
                .hasMessageContaining("Sabor do Cerrado");
    }

    @Test
    @DisplayName("3. Busca por categoria ignora maiusculas e minusculas (R5)")
    void caso3_categoria() {
        assertThat(restauranteRepository.findByCategoriaIgnoreCase("goiana"))
                .extracting(Restaurante::getNome).containsExactly("Sabor do Cerrado");
    }

    @Test
    @DisplayName("4. Busca por bairro consulta campo do objeto incorporado (R5)")
    void caso4_bairro() {
        servico.cadastrarRestaurante(new Restaurante("Nonna Pasta", "Italiana",
                new Endereco("Rua 9, 20", "Setor Oeste", "Goiania")));
        assertThat(restauranteRepository.findByEnderecoBairro("Setor Oeste"))
                .extracting(Restaurante::getNome).containsExactly("Nonna Pasta");
    }

    // ---------------- Casos escritos pelo estudante (Etapa 5) ----------------

    @Test
    @DisplayName("5. Item e incluido no cardapio; codigo repetido e rejeitado (R2)")
    void caso5_adicionarItem() {
        servico.adicionarItemCardapio(cerrado.getId(),
                new ItemCardapio("PAO_QUEIJO", "Pao de queijo", new BigDecimal("8.00"), true));

        Restaurante comItemNovo = servico.buscarRestaurante(cerrado.getId());
        assertThat(comItemNovo.getCardapio()).hasSize(4);
        assertThat(comItemNovo.buscarItem("PAO_QUEIJO").orElseThrow().preco())
                .isEqualByComparingTo("8.00");

        assertThatThrownBy(() -> servico.adicionarItemCardapio(cerrado.getId(),
                new ItemCardapio("PAMONHA", "Pamonha doce", new BigDecimal("14.00"), true)))
                .isInstanceOf(ItemDuplicadoException.class)
                .hasMessageContaining("PAMONHA");

        Restaurante semAlteracao = servico.buscarRestaurante(cerrado.getId());
        assertThat(semAlteracao.getCardapio()).hasSize(4);
        assertThat(semAlteracao.buscarItem("PAMONHA").orElseThrow().nome()).isEqualTo("Pamonha de sal");
        assertThat(semAlteracao.buscarItem("PAMONHA").orElseThrow().preco()).isEqualByComparingTo("12.00");
    }

    @Test
    @DisplayName("6. Pedido copia nome e preco e calcula o total (R3)")
    void caso6_criarPedido() {
        Pedido criado = servico.criarPedido(cerrado.getId(), ana, List.of(
                new ItemSolicitado("PAMONHA", 2),
                new ItemSolicitado("EMPADAO", 1)));

        Pedido lido = servico.buscarPedido(criado.getId());
        assertThat(lido.getStatus()).isEqualTo(StatusPedido.RECEBIDO);
        assertThat(lido.getTotal()).isEqualByComparingTo("49.50");
        assertThat(lido.getItens())
                .extracting(ItemPedido::nome)
                .containsExactly("Pamonha de sal", "Empadao goiano");
        assertThat(lido.getItens().get(0).precoUnitario()).isEqualByComparingTo("12.00");
        assertThat(lido.getItens().get(1).precoUnitario()).isEqualByComparingTo("25.50");
    }

    @Test
    @DisplayName("7. Pedido vazio, item indisponivel ou inexistente sao rejeitados (R3)")
    void caso7_pedidoInvalido() {
        assertThatThrownBy(() -> servico.criarPedido(cerrado.getId(), ana, List.of()))
                .isInstanceOf(PedidoInvalidoException.class);

        assertThatThrownBy(() -> servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("PAMONHA", 0))))
                .isInstanceOf(PedidoInvalidoException.class);

        assertThatThrownBy(() -> servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("PEQUI", 1))))
                .isInstanceOf(ItemIndisponivelException.class)
                .hasMessageContaining("PEQUI");

        assertThatThrownBy(() -> servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("FEIJOADA", 1))))
                .isInstanceOf(ItemIndisponivelException.class)
                .hasMessageContaining("FEIJOADA");

        assertThat(pedidoRepository.count()).isZero();
    }

    @Test
    @DisplayName("8. Alteracao de preco no cardapio nao afeta pedido existente (R4)")
    void caso8_snapshotDePreco() {
        Pedido pedido = servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("PAMONHA", 2)));

        servico.alterarPreco(cerrado.getId(), "PAMONHA", new BigDecimal("15.00"));

        Restaurante restaurante = servico.buscarRestaurante(cerrado.getId());
        assertThat(restaurante.buscarItem("PAMONHA").orElseThrow().preco()).isEqualByComparingTo("15.00");

        Pedido lido = servico.buscarPedido(pedido.getId());
        assertThat(lido.getTotal()).isEqualByComparingTo("24.00");
        assertThat(lido.getItens().get(0).precoUnitario()).isEqualByComparingTo("12.00");
    }

    @Test
    @DisplayName("9. Transicoes de status respeitam o ciclo de vida (R6)")
    void caso9_transicoes() {
        Pedido pedido = servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("EMPADAO", 1)));
        String id = pedido.getId();

        assertThat(servico.avancarStatus(id).getStatus()).isEqualTo(StatusPedido.EM_PREPARO);
        assertThatThrownBy(() -> servico.cancelarPedido(id))
                .isInstanceOf(TransicaoInvalidaException.class);

        assertThat(servico.avancarStatus(id).getStatus()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA);
        assertThat(servico.avancarStatus(id).getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThatThrownBy(() -> servico.avancarStatus(id))
                .isInstanceOf(TransicaoInvalidaException.class);

        Pedido outro = servico.criarPedido(cerrado.getId(), ana,
                List.of(new ItemSolicitado("PAMONHA", 1)));
        assertThat(servico.cancelarPedido(outro.getId()).getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThatThrownBy(() -> servico.avancarStatus(outro.getId()))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    // ---------------- Caso fornecido (agregacao) ----------------

    @Test
    @DisplayName("10. Faturamento agrega apenas pedidos ENTREGUE, em ordem decrescente (R7)")
    void caso10_faturamento() {
        Restaurante nonna = servico.cadastrarRestaurante(new Restaurante("Nonna Pasta", "Italiana",
                new Endereco("Rua 9, 20", "Setor Oeste", "Goiania")));
        servico.adicionarItemCardapio(nonna.getId(),
                new ItemCardapio("LASANHA", "Lasanha bolonhesa", new BigDecimal("48.00"), true));

        pedidoEntregue(cerrado.getId(), "PAMONHA", 2);   // 24.00
        pedidoEntregue(cerrado.getId(), "EMPADAO", 1);   // 25.50
        pedidoEntregue(nonna.getId(), "LASANHA", 2);     // 96.00
        servico.criarPedido(nonna.getId(), ana, List.of(new ItemSolicitado("LASANHA", 5))); // nao entregue

        List<FaturamentoRestaurante> relatorio = servico.faturamentoPorRestaurante();

        assertThat(relatorio).hasSize(2);
        assertThat(relatorio.get(0).restauranteId()).isEqualTo(nonna.getId());
        assertThat(relatorio.get(0).faturamento()).isEqualByComparingTo("96.00");
        assertThat(relatorio.get(0).quantidadePedidos()).isEqualTo(1);
        assertThat(relatorio.get(1).restauranteId()).isEqualTo(cerrado.getId());
        assertThat(relatorio.get(1).faturamento()).isEqualByComparingTo("49.50");
        assertThat(relatorio.get(1).quantidadePedidos()).isEqualTo(2);
    }
}