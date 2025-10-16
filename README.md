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

![Diagrama de Classes](docs/DiagramaClasses-DummyJSON.svg)

![Diagrama de Pacotes](docs/DiagramaPacotes-DummyJSON.svg)

![Diagrama de Sequencia](docs/DiagramaSequencia-DummyJSON.svg)

![Fluxo de Dependencia](docs/FluxoDependencia-DummyJSON.svg)
