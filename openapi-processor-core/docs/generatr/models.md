---
layout: default
title: Models
parent: The generatr
nav_order: 7
---

# Models

The generatr will create simple POJOs classes for the `object` schemas used in the OpenAPI description.
A POJO will only have (annotated) properties and get/set methods for its properties. 

The following api describes two endpoints:

- the first one `/book-inline` defines the response **schema** *inline*. This is interesting because the
api does not provide a `schema` name.

- the second one `/book` references a **named schema**. 

```yaml
openapi: 3.0.2
info:
  title: model exmaple
  version: 1.0.0

paths:
  /book-inline:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
                schema:
                  type: object
                  properties:
                    isbn:
                      type: string
                    title:
                      type: string

  /book:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
                schema:
                  $ref: '#/components/schemas/Book'

components:
  schemas:
    Book:
      type: object
      properties:
        isbn:
          type: string
        title:
          type: string
```

The second endpoint uses a **schema** with name so the generatr can simply create a POJO using the
name as the Java class name. 

```java
package generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("title")
    private String title;

    public String getIsbn () {
        return isbn;
    }

    public void setIsbn (String isbn) {
        this.isbn = isbn;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

}
```

The first endpoint has no name so the generatr invents a name based on the endpoint description.
In this case the name will be `BookInlineResponse200`. To create a unique name and avoid name collisions
with other inline objects it is created by concatenating 

- the path of the endpoint, `/book-inline` is mapped to `BookInline`
- `Response`, because it is an inline object described under `responses:`
- `200`, which is the the http status code io the response    

which is finally the bulky `BookInlineResponse200`.

Apart from the generated name it will have exactly the same content (i.e. properties & setter/getter)
since the schema description is identical.

