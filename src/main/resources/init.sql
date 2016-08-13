CREATE TABLE order_products
(
    ORDER_ID INT(11) NOT NULL,
    QUANTITY INT(11),
    PRODUCT_ID INT(11) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (ORDER_ID, PRODUCT_ID),
    CONSTRAINT FK_52n83wo48soniiymtbvu8402u FOREIGN KEY (PRODUCT_ID) REFERENCES products (ID),
    CONSTRAINT FK_q176v1ugc046idlhvw736y6by FOREIGN KEY (ORDER_ID) REFERENCES orders (ID)
);
CREATE INDEX FK_52n83wo48soniiymtbvu8402u ON order_products (PRODUCT_ID);
CREATE TABLE orders
(
    ID INT(11) PRIMARY KEY NOT NULL,
    STATUS VARCHAR(20),
    OWNER_ID INT(11),
    CONSTRAINT FK_dh50iai6gnosxu9wkqrgr769w FOREIGN KEY (OWNER_ID) REFERENCES users (ID)
);
CREATE INDEX FK_dh50iai6gnosxu9wkqrgr769w ON orders (OWNER_ID);
CREATE TABLE products
(
    ID INT(11) PRIMARY KEY NOT NULL,
    NAME VARCHAR(255),
    PRICE BIGINT(20)
);
CREATE TABLE user_orders
(
    USER_ID INT(11) NOT NULL,
    ORDER_ID INT(11) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (USER_ID, ORDER_ID),
    CONSTRAINT FK_2f59a5aos75rw7718n2medble FOREIGN KEY (ORDER_ID) REFERENCES orders (ID),
    CONSTRAINT FK_tbcrhr4f3mrfouinv2ipqdy27 FOREIGN KEY (USER_ID) REFERENCES users (ID)
);
CREATE UNIQUE INDEX UK_2f59a5aos75rw7718n2medble ON user_orders (ORDER_ID);
CREATE TABLE users
(
    ID INT(11) PRIMARY KEY NOT NULL,
    ADMIN BIT(1),
    BLOCKED BIT(1),
    LOGIN VARCHAR(255) UNIQUE,
    PASSWORD VARCHAR(255)
);
