package com.msvc.order.service;

import com.msvc.order.dto.InventarioResponse;
import com.msvc.order.dto.OrderLineItemsDto;
import com.msvc.order.dto.OrderRequest;
import com.msvc.order.model.Order;
import com.msvc.order.model.OrderLineItems;
import com.msvc.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service// se encarga de la lógica de negocio.
@Transactional
public class OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired

    private WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest){

        Order order = new Order();
        order.setNumeroPedido(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()//obtener la lista  de productos
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItems(orderLineItems);

        List<String> codigoSku = order.getOrderLineItems().stream()//obtener la lista de los codigos sku
                        .map(OrderLineItems::getCodigoSku)
                                .collect(Collectors.toList());
        System.out.println("codigos sku " + codigoSku);

        InventarioResponse[] inventarioResponseArray =  webClientBuilder.build().get()
                        .uri("http://localhost:8082/api/inventario", uriBuilder -> uriBuilder.queryParam("codigoSku", codigoSku).build())
                        .retrieve()
                        .bodyToMono(InventarioResponse[].class)
                        .block();

        boolean allProductosInStock = Arrays.stream(inventarioResponseArray)
                        .allMatch(InventarioResponse::isInStock);

        if(allProductosInStock){
            orderRepository.save(order);
        }
        else{
            throw new IllegalArgumentException("El producto no está en stock");
        }

    }

private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){

    OrderLineItems orderLineItems = new OrderLineItems();
    orderLineItems.setPrecio(orderLineItemsDto.getPrecio());
    orderLineItems.setCantidad(orderLineItemsDto.getCantidad());
    orderLineItems.setCodigoSku(orderLineItemsDto.getCodigoSku());

    return orderLineItems;

}


}
