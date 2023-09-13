# Reactive microservice webhook

Dependencies:

* OpenJDK Java version >= 17
* [Docker](https://www.docker.com)
* [Docker Compose](https://docs.docker.com/compose/)
* [Maven](https://maven.apache.org/)
* [RabbitMQ](https://www.rabbitmq.com/)

Note: Context propagation seems still experimental and even
using `Hooks.enableAutomaticContextPropagation();` some traces are not being propagated within
nested context.

## Run project

```bash
make start-infra
```

```bash
make run
```

## Rabbitmq dashboard

- login: `user`
- password: `password`

## Send webhook

![Webhook](https://github.com/Jojoooo1/spring-microservice-webhook-reactive/blob/main/scripts/config/webhook.png)
