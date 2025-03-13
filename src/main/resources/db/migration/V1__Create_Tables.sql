create table user_
(
    id       serial unique primary key,
    name     varchar(255) not null unique,
    password varchar(255) not null
);

create table task
(
    id       serial       unique primary key,
    text     varchar(255) not null,
    done     boolean      not null,
    owner_id int          not null references user_ (id)
);
