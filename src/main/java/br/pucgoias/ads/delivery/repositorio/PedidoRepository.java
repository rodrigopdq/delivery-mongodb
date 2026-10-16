package br.pucgoias.ads.delivery.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.pucgoias.ads.delivery.dominio.Pedido;
import br.pucgoias.ads.delivery.dominio.StatusPedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    /** Pedidos de um restaurante, do mais recente para o mais antigo. */
    List<Pedido> findByRestauranteIdOrderByCriadoEmDesc(String restauranteId);

    /** Pedidos por status. */
    List<Pedido> findByStatus(StatusPedido status);

    /** Pedidos pelo telefone do cliente (campo do objeto incorporado). */
    @Query("{ 'cliente.telefone': ?0 }")
    List<Pedido> buscarPorTelefoneDoCliente(String telefone);
}