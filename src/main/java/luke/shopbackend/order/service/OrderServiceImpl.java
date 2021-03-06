package luke.shopbackend.order.service;


import luke.shopbackend.exception.OrderNotFoundException;
import luke.shopbackend.order.model.dto.CustomerOrderRequest;
import luke.shopbackend.order.model.embeddable.CartItem;
import luke.shopbackend.order.model.entity.Customer;
import luke.shopbackend.order.model.entity.CustomerOrder;
import luke.shopbackend.user.model.User;
import luke.shopbackend.user.service.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final CustomerOrderRepository customerOrderRepository;
    private final FormatCustomerOrder format;
    private final UserRepository userRepository;


    public OrderServiceImpl(
            CustomerOrderRepository customerOrderRepository,
            FormatCustomerOrder formatCustomerOrder, UserRepository userRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.format = formatCustomerOrder;
        this.userRepository = userRepository;
    }

    /**
     * Only for ROLE_ADMIN.
     */
    @Override
    public Page<CustomerOrder> getAllPageable(Pageable pageable) {
        return customerOrderRepository.findAll(pageable);
    }

    /**
     * @param id of CustomerOrder.
     * @return one order of CustomerOrder entity.
     */
    @Override
    public CustomerOrder getOrder(Long id) {
        return customerOrderRepository.getCustomerOrderByOrderId(id).orElseThrow(
                () -> new OrderNotFoundException("Nie znaleziono zamówienia o numerze: " + id));
    }

    /**
     * Calls out OrderService().getOrder(Long id) to check if the order exists, then deletes the CustomerOrder.
     */
    @Override
    public void deleteCustomerOrderByOrderId(Long id) {
        CustomerOrder order = getOrder(id);
        customerOrderRepository.delete(order);
    }

    /**
     * @param orderRequest -> it contains purchase total value and quantity, besides that dto classes like
     *                     CustomerDto - containing credentials and address.
     *                     CartItemValidateDto - containing items purchased.
     *                     <p>
     *                     The whole idea is that after validation, map this data to entity classes and save them. So:
     *                     CartItemValidateDto data goes to -> CartItem. And CartItem is composed to -> CustomerOrder
     *                     CustomerDto data goes to -> Customer
     *                     If we didn't ran out of items the customer and order are persisted. Else we send a response
     *                     to the client we have no items left.
     */
    @Override
    public CustomerOrder addOrder(CustomerOrderRequest orderRequest) {
        List<CartItem> cartItems = format.getCartItems(orderRequest);
        Customer customer = format.getCustomerObject(orderRequest);
        CustomerOrder customerOrder = format.getCustomerOrder(cartItems, orderRequest);

        customerOrder.setCustomer(customer);

        if (customerOrder.getTotalPrice().equals(BigDecimal.valueOf(0)) && customerOrder.getTotalQuantity() == 0)
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT,
                    "Nie ma już przedmiotu/przedmiotów umieszczonych w koszyku");
        else
            return orderRequest.getUsername() != null ?
                    saveOrderRegisteredUser(customerOrder, orderRequest.getUsername())
                    : saveOrderForeignUser(customerOrder);
    }

    /**
     * Saves order of unregistered user.
     */
    protected CustomerOrder saveOrderForeignUser(CustomerOrder customerOrder) {
        return customerOrderRepository.save(customerOrder);
    }

    /**
     * Saves order of registered user.
     */
    protected CustomerOrder saveOrderRegisteredUser(CustomerOrder customerOrder, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nie znaleziono w bazie użytkownika o nazwie: " + username));

        customerOrder.setUser(user);
        return customerOrderRepository.save(customerOrder);
    }
}
