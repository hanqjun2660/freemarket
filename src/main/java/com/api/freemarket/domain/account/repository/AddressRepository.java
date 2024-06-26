package com.api.freemarket.domain.account.repository;

import com.api.freemarket.domain.account.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
