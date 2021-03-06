package com.example.eshop.customer.application.signup;

import com.example.eshop.customer.domain.customer.*;
import com.example.eshop.sharedkernel.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {
    private final CustomerRepository customerRepository;
    private final HashedPasswordFactory hashedPasswordFactory;
    private final UniqueEmailSpecification uniqueEmailSpecification;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Customer signUp(SignUpCommand command) {
        var customer = createCustomer(command);

        customerRepository.save(customer);

        eventPublisher.publishEvent(new CustomerCreatedEvent(customer.getId().toString()));

        return customer;
    }

    private Customer createCustomer(SignUpCommand command) {
        var email = Email.fromString(command.email());

        if (!uniqueEmailSpecification.isSatisfiedBy(email)) {
            throw new EmailAlreadyExistException("Customer with e-mail " + email + " already exists");
        }

        return Customer.builder()
                .firstname(command.firstname())
                .lastname(command.lastname())
                .birthday(command.birthday())
                .email(email)
                .password(hashedPasswordFactory.createFromPlainPassword(command.password()))
                .build();
    }
}
