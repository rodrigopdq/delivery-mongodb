package br.pucgoias.ads.delivery.servico;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.Decimal128;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

import br.pucgoias.ads.delivery.dominio.Cliente;
import br.pucgoias.ads.delivery.dominio.FaturamentoRestaurante;
import br.pucgoias.ads.delivery.dominio.ItemCardapio;
import br.pucgoias.ads.delivery.dominio.ItemPedido;
import br.pucgoias.ads.delivery.dominio.Pedido;
import br.pucgoias.ads.delivery.dominio.Restaurante;
import br.pucgoias.ads.delivery.dominio.StatusPedido;
import br.pucgoias.ads.delivery.excecao.ItemDuplicadoException;
import br.pucgoias.ads.delivery.excecao.ItemIndisponivelException;
import br.pucgoias.ads.delivery.excecao.PedidoInvalidoException;
import br.pucgoias.ads.delivery.excecao.RecursoNaoEncontradoException;
import br.pucgoias.ads.delivery.excecao.RestauranteDuplicadoException;
import br.pucgoias.ads.delivery.repositorio.PedidoRepository;
import br.pucgoias.ads.delivery.repositorio.RestauranteRepository;

@Service
public class DeliveryService {

    private final RestauranteRepository restauranteRepository;
    private final PedidoRepository pedidoRepository;
    private final MongoTemplate mongoTemplate;

    public DeliveryService(RestauranteRepository restauranteRepository,
                           PedidoRepository pedidoRepository,
                           MongoTemplate mongoTemplate) {
        this.restauranteRepository = restauranteRepository;
        this.pedidoRepository = pedidoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // ---------------------------------------------------------------
    // Operacoes fornecidas (referencia)
    // ---------------------------------------------------------------

    /** R1: o nome do restaurante e unico (garantido pelo indice unico). */
    public Restaurante cadastrarRestaurante(Restaurante restaurante) {
        try {
            return restauranteRepository.save(restaurante);
        } catch (DuplicateKeyException e) {
            throw new RestauranteDuplicadoException(restaurante.getNome());
        }
    }

    public Restaurante buscarRestaurante(String id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Restaurante", id));
    }

    public Pedido buscarPedido(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    /**
     * Altera o preco de um item do cardapio com o operador posicional "$":
     * { $set: { "cardapio.$.preco": novoPreco } }
     */
    public void alterarPreco(String restauranteId, String codigo, BigDecimal novoPreco) {
        Query query = Query.query(Criteria.where("id").is(restauranteId)
                .and("cardapio.codigo").is(codigo));
        Update update = new Update().set("cardapio.$.preco", new Decimal128(novoPreco));
        UpdateResult resultado = mongoTemplate.updateFirst(query, update, Restaurante.class);
        if (resultado.getMatchedCount() == 0) {
            throw new RecursoNaoEncontradoException("Item " + codigo + " do restaurante", restauranteId);
        }
    }

    // ---------------------------------------------------------------
    // Operacoes a implementar (Etapa 4)
    // ---------------------------------------------------------------

    /**
     * R2: incluir o item no cardapio com UMA atualizacao atomica ($push),
     * condicionada a inexistencia do codigo no array ($ne).
     * Restaurante inexistente: RecursoNaoEncontradoException.
     * Codigo repetido: ItemDuplicadoException.
     */
    public void adicionarItemCardapio(String restauranteId, ItemCardapio item) {
        throw new UnsupportedOperationException("TODO: adicionarItemCardapio");
    }

    /**
     * R3 e R4: rejeitar lista vazia ou quantidade nao positiva (PedidoInvalidoException);
     * rejeitar item inexistente ou indisponivel (ItemIndisponivelException);
     * copiar nome e preco vigentes do cardapio para cada ItemPedido.
     */
    public Pedido criarPedido(String restauranteId, Cliente cliente, List<ItemSolicitado> solicitados) {
        throw new UnsupportedOperationException("TODO: criarPedido");
    }

    /** R6: avancar o pedido para o proximo status. */
    public Pedido avancarStatus(String pedidoId) {
        throw new UnsupportedOperationException("TODO: avancarStatus");
    }

    /** R6: cancelar o pedido (somente no status RECEBIDO). */
    public Pedido cancelarPedido(String pedidoId) {
        throw new UnsupportedOperationException("TODO: cancelarPedido");
    }

    /**
     * R7: pipeline de agregacao sobre "pedidos":
     * $match (status ENTREGUE) -> $group (restauranteId: soma de total e contagem)
     * -> $project (renomear _id para restauranteId) -> $sort (faturamento decrescente).
     */
    public List<FaturamentoRestaurante> faturamentoPorRestaurante() {
        throw new UnsupportedOperationException("TODO: faturamentoPorRestaurante");
    }
}
