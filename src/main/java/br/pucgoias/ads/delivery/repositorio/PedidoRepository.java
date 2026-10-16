package br.pucgoias.ads.delivery.repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.pucgoias.ads.delivery.dominio.Pedido;

/**
 * TODO (Etapa 3): declarar as consultas:
 *   - pedidos de um restaurante, do mais recente para o mais antigo (consulta derivada);
 *   - pedidos por status (consulta derivada);
 *   - pedidos pelo telefone do cliente (@Query em JSON sobre campo incorporado).
 */
public interface PedidoRepository extends MongoRepository<Pedido, String> {
}
