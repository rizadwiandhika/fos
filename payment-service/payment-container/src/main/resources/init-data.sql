INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
VALUES ('16638397-41d5-4215-beae-9e787e5e5bab', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 500.00);

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('6325fd2a-eb48-4301-92b1-d865dbdbeef5', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 100.00, 'CREDIT');
INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('3a7047fc-e985-4e99-be0e-d82be5ccd24e', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 600.00, 'CREDIT');
INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('a51cf7de-50cb-4610-b8d4-aaf1bddaf4fa', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 200.00, 'DEBIT');


INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
VALUES ('c91c6f84-b441-4819-a07a-50d3c28f2980', 'd215b5f8-0249-4dc5-89a3-51fd148cfb42', 100.00);

INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('b500e543-06df-498f-a10c-7e3068d0eb02', 'd215b5f8-0249-4dc5-89a3-51fd148cfb42', 100.00, 'CREDIT');