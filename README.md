# 📦 Catálogo & Tarefas - DummyJSON Console App

Aplicação console Java que consome a API pública [DummyJSON](https://dummyjson.com) para gerenciar um catálogo de produtos e uma lista de tarefas (todos), construída seguindo os princípios de **Clean Architecture** e **SOLID**.

## 🎯 Objetivo

Criar uma aplicação Java (sem frameworks) que:
- Modela classes simples do domínio
- Consome endpoints REST da DummyJSON para consultas e ações CRUD
- Fornece um menu interativo no console
- Demonstra princípios de arquitetura limpa e código de qualidade

## 🏗️ Arquitetura

Este projeto implementa **Clean Architecture** (Arquitetura Limpa) com separação clara de responsabilidades em camadas concêntricas, onde as dependências apontam sempre para dentro (em direção ao domínio).

### 📐 Estrutura de Camadas

```
┌─────────────────────────────────────────────┐
│         ADAPTER (Console/UI)                │  ← Camada mais externa
├─────────────────────────────────────────────┤
│         APPLICATION (Services)              │
├─────────────────────────────────────────────┤
│         DOMAIN (Entities + Ports)           │  ← Core (Regras de negócio)
├─────────────────────────────────────────────┤
│         INFRASTRUCTURE (HTTP/Repo)          │
└─────────────────────────────────────────────┘
```

### 📂 Organização do Projeto

```
com.antonio/
├── 🎯 adapter/console          # Camada de apresentação
│   └── ConsoleApp.java         # Interface do usuário (console)
│
├── 💼 application/service      # Casos de uso / Application Services
│   ├── ProductService.java     # Interface do serviço de produtos
│   ├── TodoService.java        # Interface do serviço de tarefas
│   └── impl/
│       ├── ProductServiceImpl.java
│       └── TodoServiceImpl.java
│
├── 🏛️ domain                   # CORE - Coração da aplicação
│   ├── model/                  # Entidades do domínio
│   │   ├── Product.java
│   │   ├── Todo.java
│   │   └── User.java
│   └── port/                   # Interfaces (abstrações)
│       ├── ProductRepository.java
│       └── TodoRepository.java
│
├── 📦 dto/                     # Data Transfer Objects
│   ├── ProductListResponse.java
│   └── TodoListResponse.java
│
├── ⚠️ exception/               # Tratamento de exceções
│   ├── DomainException.java
│   └── HttpException.java
│
└── ⚙️ infraestructure          # Implementações técnicas
    ├── http/
    │   └── HttpClientWrapper.java
    └── repository/
        ├── ProductRepositoryHttp.java
        └── TodoRepositoryHttp.java
```

## 🔷 Princípios SOLID Aplicados

### **S** - Single Responsibility Principle (Princípio da Responsabilidade Única)
- `ConsoleApp`: Responsável apenas pela interação com o usuário
- `ProductServiceImpl`: Responsável apenas pela lógica de produtos
- `HttpClientWrapper`: Responsável apenas por fazer requisições HTTP
- Cada classe tem uma única razão para mudar

### **O** - Open/Closed Principle (Princípio Aberto/Fechado)
- As interfaces (`ProductRepository`, `TodoRepository`) estão abertas para extensão
- Podemos criar novas implementações (ex: `ProductRepositoryDatabase`) sem modificar o código existente
- Services dependem de abstrações, não de implementações concretas

### **L** - Liskov Substitution Principle (Princípio da Substituição de Liskov)
- Qualquer implementação de `ProductRepository` pode substituir outra sem quebrar o sistema
- `ProductRepositoryHttp` pode ser substituído por `ProductRepositoryMock` em testes

### **I** - Interface Segregation Principle (Princípio da Segregação de Interface)
- Interfaces pequenas e específicas: `ProductRepository` e `TodoRepository` separadas
- Clientes não são forçados a depender de métodos que não usam

### **D** - Dependency Inversion Principle (Princípio da Inversão de Dependência)
- `ProductServiceImpl` depende da interface `ProductRepository`, não da implementação
- `ConsoleApp` depende de `ProductService` e `TodoService` (abstrações)
- Módulos de alto nível não dependem de módulos de baixo nível
- **Inversão de Controle**: As dependências são injetadas via construtor

## 🚀 Funcionalidades

### 📦 Produtos (ProductService)
- `list(limit, skip)` - Lista produtos com paginação
- `search(query)` - Busca produtos por texto

### ✅ Tarefas (TodoService)
- `list(limit, skip)` - Lista todos com paginação
- `add(text, userId)` - Adiciona novo todo
- `toggle(id, completed)` - Marca/desmarca todo como completo
- `delete(id)` - Remove todo

### 🖥️ Menu Console
1. Listar produtos (com limit e skip)
2. Buscar produtos por texto
3. Listar todos (com limit e skip)
4. Adicionar todo
5. Marcar/Desmarcar todo
6. Remover todo
7. Sair

## 🛠️ Tecnologias e Restrições

- ☕ **Java 17+** - Sem frameworks (Spring, etc.)
- 📚 **Lombok** - Redução de boilerplate (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- 🔄 **Jackson** - Serialização/Deserialização JSON
- 🔗 **HttpURLConnection** - Cliente HTTP nativo Java
- 📦 **Maven** - Gerenciamento de dependências (pom.xml)

## 📋 Requisitos de Qualidade

### ✅ Tratamento de Erros
- **IO**: Tratamento de problemas de rede e conexão
- **JSON Inválido**: Validação de respostas da API
- **Status HTTP ≠ 2xx**: Lançamento de `HttpException` com status code
- **Entradas vazias**: Validação de inputs do usuário

### 📝 Código Legível
- Nomes claros e descritivos
- Uso de `toString()` para entidades (facilitado por Lombok)
- Mensagens amigáveis ao usuário no console
- Separação clara de responsabilidades

### 🏗️ Build
- `pom.xml` configurado com Jackson e Lombok
- Compilação: `mvn clean compile`
- Execução: `mvn exec:java -Dexec.mainClass="com.antonio.adapter.console.ConsoleApp"`

## 🌐 Endpoints Utilizados (DummyJSON)

### Produtos
- `GET /products?limit={n}&skip={k}` - Listagem com paginação
- `GET /products/search?q={termo}` - Busca por texto

### Todos
- `GET /todos?limit={n}&skip={k}` - Listagem com paginação
- `POST /todos/add` - Adicionar
- `PUT /todos/{id}` - Atualizar (marcar/desmarcar)
- `DELETE /todos/{id}` - Remover

## 🧪 Exemplo de Uso

```
========================================
   CATÁLOGO & TAREFAS - DUMMYJSON
========================================
1. Listar produtos
2. Buscar produtos por texto
3. Listar todos
4. Adicionar todo
5. Marcar/Desmarcar todo
6. Remover todo
7. Sair

Escolha uma opção: 1

Digite o limit (padrão 10): 5
Digite o skip (padrão 0): 0

```

## 🎓 Benefícios da Clean Architecture

1. **Testabilidade**: Cada camada pode ser testada isoladamente
2. **Manutenibilidade**: Mudanças são localizadas e controladas
3. **Independência de Frameworks**: Core não depende de tecnologias externas
4. **Independência de UI**: Console pode ser substituído por Web/Desktop
5. **Independência de Database**: HTTP pode ser substituído por SQL/NoSQL
6. **Regras de Negócio Protegidas**: Domain é isolado de detalhes técnicos

## 📊 Diagramas

Os diagramas UML (Classes, Pacotes e Sequência) estão disponíveis na documentação do projeto e ilustram:
- Relacionamentos entre classes
- Fluxo de dependências (sempre apontando para o core)
- Sequência de operações end-to-end

![Diagrama de Classes](https://mermaid.ink/svg/pako:eNrFWG1v2zYQ_isCgQIyohh2ayexEATIkg0z0K5BkmLA4C-MyNhEZZIjqS5elv72HvVmimJSZytmf7F0fJ574_GO9iPKBKEoRVmOtb5keKnwesEj-Lx5E12KNWY8eo83VFXCEhZdKUGKzESPldB-Dt8LvowYcSQ3RjGQGWZy6ogvRXGX00gqljnig1pnzEhSUZIKMnAwS2rmJB5E1lZXfGsZsFLZ7K5dWTWwVhmu1p4W3A3oVhCxYzSAdKQ_CZFTzKNMrGVODSW-jkJTNXekB9ZUFSQ8JFtiUkN3jNdqCYTL9EWjEJZr77rUT6WZjlYvGxaxWzbumdLmN7ym_SXQ5K0cWL1l6C0taWG7hf1LkecW7YXe-g81eyWU0VE854aqe5xRPQhU7jWVQjMj1MaN8_SUNayzM8dwzrSJc7ZmJon0ZybBM5B8rXV9dZCaYpWt4j8LqjZBVKDufogvVpHrCCYkNvTBtFVVmnIARiyXcGLsbrQl2AMRauUAGkR3bim5-b68_agDGbZeXVMtBde0W0tuUuCIlw_aAUDccDYMzj2ZjdYTlXno9xDXdtxYSCqlVdaSijp4dlNedr_MdnmAf5Djvsm41L2Ly7ADN1R9gQ4X2oV6af9F_t8d2VOFh9I5B22douh3FdU-9svT0RJvcYP_ey96UXjNKBiCx3-d__trUnN-r2DQKMhLoaibkF-NkRc5o9z8rrCU3uCrZ9kd1vSTcs71QY8W1xhvjsWUEymg0AOzGjK3XU_AebIJoYodQHUOwsbCtbzdaRtLJ-p-TlatpF_OXUXxFjrYwwj9t7H0tbwukP2V9jnB0nSv5xcwQgB9LmWoSzWtWHZevT7QgMz22T0WGeYcUqmrb8fjrem4qz9xVbkJVQWH29wX0bleEqZljjcfKC_6iyvMSU7raM4zw8DmcygbSwjiZvDnh4zKEuImsfrt0651JxdtxJ3J5VHiNdUaL2n4lmFr7DXaq6uEwaaAKz6hXi_qGU0crNeTbtoFSAlrjoGbkWua4zIhKybrpHjFc_rPcBiaimnEbDGv4dTURLeaSpY_gvqU_jh1zXmHPWzR5wa6RJcYirJ07_DwLOBQGhEqod3qSPBenC3P86RLeibW0jdL7neu1DaTYJA7kF4yNxy2MQI-UxSbF-xYdPmD2YN-V3fnUr27nedoFdFpds5WNRXnpsxDuoUZTJK7m9YXvyOkkVkp8ZcO7__3Gf29spxuX2gYKEFLxQhK4Q5DE7SmCjTDKyobxwKZFRTyAqXwSLD6vEAL_gQcifkfQqwbmhLFcoXSe5xreCskgUTW__m0UgUFStWFKLhB6XQ8KZWg9BE9oPRwOh0ejU-mR7PZeDqazCajowRtQD4ejybDyXR8PB4fH80m705OnhL0d2n57XDy9t34-Hg2mkxGs9k0QZTYPf5Q_-9kv56-AYHD65I)

![Diagrama de Pacotes](https://mermaid.ink/svg/pako:eNqNVUtu2zAQvQrBIEAC2K4-tmQJRQBZshEDSSzYMoq27kKR6FioLBKU1MYNcoUGaFdtF9l1220XPU0v0Byh1Mf6OIoRbUTO47x5Q86QN9DBLoIqvKI2WQFrsAgA-8L4MjNork0iRN8u4MP9519AMzTTGk7BmfZ6OF3Ad9nq5NNxEGIfsYVONnp5SV-c5FaNkGIxCtxFsBuFEN9z7MjDQRrpyx-gmebZWNes8eSiIdoM0Q-eg0K2OsyGaTiTYjd2olnFZGEX5_NGhvGa-CVLx2PTBqrx1lyhS2x7szLw2vayhO5-_Pt9B4zJuTbO0wFHOqbouKbpnB1FImad_KsqitDpYM7E1vxMTCPmRtiv6jVFBIdehOmm8C9Ne5WPgyW1w4gykpgmZ_r3-7ckgfHFaKrNrOlct-bTYcO5nFqWyZavooikMU_ZQPc9FESvGDHZ0V2qYT60rvZRDqdbznoeiXlvLrOYkGx_Hu6__gSzuWlOplZNh2FNGOxGuBr5zAtZ9JCwCi4rqWqsUQyvHUTy-kXbceqWVcGwZktEF5Ym9YeHwECEGVDgeCgERzhmXQh8e4NoCNwUAjgAXhAU5uPMNW850G6fFEWeIdWST-GkbpqgziPXcr8ztPSsIgxICqARYHuc2esBMyzdoidEpk3RSFlBqkmXG9tMuIPvkFbQvJCiDSPOJIKl5_vqQVfXRj2uxRoEv0fqgTCUDVHIp-2PnhutVJFctxzsY6oeLJfLKlPlrsvpBF6RRmJBx_ekns7V6YQn6eqtmjOORkqfKwUOpR7PPZsxv_JzKkUX5EFJJWn8QNGeS5X3Xk4lcbLRHxRUotyVu6N9VLDFniXPhSpLD7XgGlF2CGwKb5IgCxit0Jq1ocqGrk3fL-AiuGU-xA7eYLzeulEcX62gurT9kM1i4toRMjyb3Q7lkqTTqI7jIIKqxHMpB1Rv4DVURV7p9Pt8V1a6oiLKUq8FN1Bti3KnL4mcIHR5oSf3Bf62BT-lUfkO1-UUjpdEUe7LgsQLLYjcpMLOs6c2fXFv_wO7F3sk)

![Diagrama de Sequencia](https://mermaid.ink/svg/pako:eNqFVs1S2zAQfhWNDp0wDRAnIT8eoMOQtNBSYIBOZzq-CHsJGmzLlWQmIeRl2gOnPkVerCvZSRxiQw5JJH377Wr327Wn1BcBUJcq-J1C7MOAs5FkkRcT_DBfC0l-KJDZOmFSc58nLNbkWMRKhECYWvw9SpJN2DXIR-5b2KUUQerrfOc0SsJN-KWQuoC9gkQojkFMNqHmrBR6onVJICc3N5cGbk6PQw6x_ilZkpRd7ejy1CAHaRRNvl5fnJsNL85wJhnbh4f5lV28Xwg-FzEjIpm_zP8K4tEzrjSTJDGBaaE8uswmf2QaFvnKdrPvfGuNWYQYkGYk5BHXBIh64El5EKfxnZARM5eY_4lAS3RaSp7n3kVOpWuWuG55t16FmAOLLPkWspgiVVKcCzQXjyAXBvUMPoAE4gAlNsFw8Vhh0vZv5e5hbSGRIEPgL2G3Sktm87lVjMFQYQCm1u_fwaCKxmaNxkYILhmBrnl0N8nEoz5ZnoNpPPtgqA6mDzOPviY0lkVCs0ZCVIdLvgxvyJts61RWUSsmFmKnpD4oJUitOR7nns0HkdvLqJuNBrn4Rj4SI8sVxgayzIuV7BWoBMsOK1AAJRdZhVDIUEYzAFQZZyF_YpbSFgsVxhYdZ2S-6WaNA5WHpUaP1s4Y7OfGh68slrKqABWiX9V1PXorjqLIK6gWQi520Pte1zpi3XFJ8342_Yi9q9j8X8AynfOAPBPNNQ7NZxwOSLa1yYEkprtdMhzzW1jOEBIL4hfHxioCCBWQoZQiG3G19nhcJ3vVGlpCX-lncX7G4vkLs3NyOPYh0dioVVKrAK1XtAK0Ua4KXFm5KqAlhcjSGEGs2AgiM1sA7_9G3j1qE8QEuU2VXzLGcUYVW7cgkbW5XtjH9NM6HUkeUFfLFOo0ApSHWdKpQXtU30MEHjXuAyYfjK8Z2uDj6JcQ0cJMinR0T907hiWv0zQJkD1_YC93pZmh8liksaZux2lbEupO6Zi6Lae_0-s57W6_3eq3up29Op1Qd7vV3el1Wo1ms-0097q9pjOr0yfr1tlptBv9htPpNPpO3-n10QIC85T9nr022LeH2X8BH6XM)

![Fluxo de Dependencia](https://mermaid.ink/svg/pako:eNqtVs2O2zYQfhWCiwAtYO9Ksi1bQhFAtuTGwa5t2DJa1O6Bkeg1EVkUKCldd71P0KDpTy5JDkGB3nvtobe-SV-geYSSlGTLv02B-mCRw2---TgzpHQPPepjaMJbhqIFuB7NQsB_cfosMzh3CWYhCqYz-OH9yx_AzaRvD4DzpeuM-oMZ_DqDi98kxmz64f2Pv_JR-ucvjNDSojXs8bXXL4GdLperp-NBX5hyAA79WbgX1_JRxCPLsN__BizbGvKQOwE7NIxpgKf504qic3xRFBAPJYSGkvOnP7iA4XWvY7m9QX-Hd8ioP37hTcUz9ZIxZi-Ih3vLKCiBXOpTAZLPA8QxBTZdIpIFf_Xu799fAXtwY_X64JPOYOR8uqPAJwx7Qipw21urEyYkITjmifz5u83ss2fs6nEutSJlVWQpyhuiLImz9Mth2WWEIxqThLKVtAr_renMbnrhnKE4YZwiZZjv6q-3b8Smev3uyBq7o0nHnYycg7wK7ulB6CdJEu3lVgJ31eyhnrjucCpsnYDgMPmCy4o22y5JfvRI5gOQkPcTkmmNsxVprlYfr0kYpcm6aKhsMZ_IdZomEiAcSrR5j4KEltvr0D2N0broqhOreTuVybeMIkDWPdlqTiW9fRzxvWLg43VW3AyS852DlFmWKCRRGnAhRVsd0hzDbNTutgMg_CjgJa9KfCBc1BNUL0XWCxA6kP6voC0Tx3iMHFV-EnBKNs9zcd8dCSMLJbruSIS9NTGS9s8d92o4GPO_iXtlO9eO66zFzZfB-ECixH1YuG-0jZNVQMLb_NzxCc5zCeYkCMyLesfqNpQKV0-fY_NCc5p2Tcun1W-InyzMenRX8WhAmXkxn8_LTEUuci5db7d16yyXdpJLluX_ICq3fEanqYberW3o1Ibe6Ci7dLWTdHvFzRi7XaOlbLPm6A1V-WjG4sBnVEZHa7a3VLqltg3rY6mKPivypjTtVnvDVWvWm_Xu-bxtGuUa38rrTky9AMWxjecgP_ShtwIFqbK_UcGYG3wULxBjaGWCBmjscW3PoCzNf-RTFaDBCv--ID40eTVwBS4x443Mp_BehJrBZMH5Z9DkQx-x5zM4Cx-4T4TCryhdFm6MprcLaM5REPNZGvkowTZB_IW03FiZ2DXr0DRMoKmrLUkCzXt4B82aaly2Wmq9adRrRq2pNypwBc1qrXnZ0muKptVVrdFsaepDBX4rw6qXSl0xFFXXFUM11JbBPbAvXkY32UeT_HZ6-AffZBQR)
