package luke.shopbackend.order.repository;

import luke.shopbackend.order.model.entity.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CustomerOrderRepository extends PagingAndSortingRepository<CustomerOrder, Long> {

    @Query(name = "CustomerOrder.getCustomerOrderByUserId")
    Page<CustomerOrder> getCustomerOrderByUserId(Long id, Pageable pageable);

    @Query(name = "CustomerOrder.getCustomerOrderByOrderId")
    Optional<CustomerOrder> getCustomerOrderByOrderId(Long id);
}
