# Simulador de Autômatos Finitos Determinísticos (AFD)

Simulador de AFD desenvolvido para o 1º Trabalho da disciplina de **Linguagens Formais e Autômatos**
(Ciência da Computação, UFERSA). O programa recebe a definição de um autômato
**A = (Q, Σ, δ, q0, F)**, rejeita definições inválidas (não determinísticas) e informa se
cadeias de entrada **pertencem ou não** à linguagem do autômato.


---

## 1. Requisitos e execução

- **Java 21** (JDK) e **Maven**.

Na pasta raiz do projeto (a mesma do `pom.xml`):

```bash
mvn compile
java -cp target/classes org.example.Main
```

Para rodar os testes automatizados:

```bash
mvn test
```

> Atenção: execute os comandos do Maven na pasta que contém o `pom.xml`, não dentro de `src/`.

Também é possível abrir o projeto no IntelliJ IDEA e executar a classe `Main`.

---

## 2. Como usar a interface

Ao iniciar, abre a janela **Simulador de Autômatos Finitos Determinísticos (AFD)**, com três áreas:

### 2.1 Definição do Autômato

| Campo | O que informar | Exemplo |
|---|---|---|
| Alfabeto | Símbolos separados por espaço (cada símbolo com **1 caractere**) | `0 1` |
| Estados | Nomes dos estados separados por espaço | `q0 q1 q2` |
| Estado Inicial | **Um** estado, que deve estar em Estados | `q0` |
| Estados de Aceitação | Zero ou mais estados, separados por espaço | `q2` |

### 2.2 Transições

Uma transição por linha, no formato `Origem Símbolo Destino`:

```
q0 0 q1
q0 1 q0
```

Cada linha representa δ(origem, símbolo) = destino.

### 2.3 Botões

- **Construir / Atualizar Autômato:** valida os campos e monta o autômato. Se a definição for inválida,
  uma janela de erro explica o motivo e **nenhum autômato fica ativo** (é preciso corrigir e construir novamente).
- **Carregar de Arquivo (.txt):** abre um seletor de arquivo, carrega e valida o autômato e preenche
  os campos da tela com o conteúdo carregado.

### 2.4 Simulação de Cadeias

1. Construa ou carregue um autômato.
2. Digite a cadeia em **Cadeia de Entrada** (para testar a **cadeia vazia ε**, deixe o campo vazio).
3. Clique em **Testar Cadeia**.

O resultado aparece abaixo, com o veredito (**ACEITA** em verde ou **REJEITADA** em vermelho), o
**caminho de estados percorrido** e uma mensagem explicando o motivo.

---

## 3. Formato do arquivo `.txt`

```
0 1            # linha 1: alfabeto
q0 q1 q2       # linha 2: estados
q0             # linha 3: estado inicial (somente um)
q2             # linha 4: estados de aceitação (zero ou mais, separados por espaço)
q0 0 q1        # linha 5 em diante: uma transição por linha (Origem Símbolo Destino)
q0 1 q0
q1 0 q1
q1 1 q2
q2 0 q1
q2 1 q0
```

O texto após `#` serve apenas de explicação e pode ser omitido no arquivo real.

**Como o programa distingue o estado inicial dos estados de aceitação:** somente pela **posição da
linha**, pois não há palavras-chave. A linha 3 é o estado inicial e a linha 4 são os estados de
aceitação. Um mesmo estado pode ser inicial e de aceitação ao mesmo tempo (basta aparecer nas duas
linhas); nesse caso, a cadeia vazia é aceita.

Regras do leitor de arquivos:

- Linhas em branco são ignoradas.
- Comentários são aceitos com `#` ou `//`, em linha própria ou no fim de uma linha.
- Se **não houver estados de aceitação** (F vazio), a linha 4 pode ser deixada vazia ou omitida.

---

## 4. Critério de aceitação de uma cadeia

A cadeia é lida símbolo a símbolo, aplicando δ a partir do estado inicial `q0`.

- A cadeia é **aceita** somente se, **depois de consumir todos os símbolos**, o autômato estiver
  em um estado de aceitação (∈ F). Passar por um estado de aceitação no meio do caminho **não basta**.
- A **cadeia vazia (ε)** é aceita se, e somente se, o estado inicial for de aceitação.
- É **rejeitada** nos casos:
  - termina em estado que não é de aceitação;
  - contém um símbolo que **não pertence ao alfabeto**;
  - chega a um par (estado, símbolo) sem transição definida (veja o item 5).

---

## 5. Validações e mensagens de erro

| Situação | Comportamento |
|---|---|
| Duas transições com a mesma origem e o mesmo símbolo (**não determinismo**) | Autômato **rejeitado**: "Autômato Não-Determinístico: O estado 'qX' já possui uma transição para o símbolo 'a'…" |
| Símbolo de transição fora do alfabeto | Rejeitado: "O símbolo 'x' não pertence ao alfabeto definido…" |
| Estado de origem ou destino inexistente | Rejeitado: "Estado de origem (…) ou destino (…) não encontrado." |
| Estado inicial ou de aceitação inexistente | Rejeitado: "O estado inicial/final '…' não existe nos estados declarados." |
| Linha de transição com formato errado | Rejeitado: "Formato de transição inválido: esperava 'Origem Simbolo Destino'…" (com o número da linha no arquivo) |
| Arquivo com menos de 3 linhas úteis | Rejeitado: "Arquivo incompleto…" |
| Testar cadeia sem autômato ativo | Aviso: "Por favor, construa ou carregue um autômato primeiro!" |

### AFD incompleto

Um AFD é **completo** quando existe transição para todo par (estado, símbolo). Este simulador aceita
autômatos incompletos, mas **avisa** quais transições estão faltando
("Aviso: AFD incompleto", listando, por exemplo, `δ(q0, 1)`). Ao simular, se a leitura chegar a uma
transição indefinida, o autômato para em um **estado poço implícito** e a cadeia é **REJEITADA**
(o caminho mostra `POÇO`).

---

## 6. Exemplos de uso

Os arquivos de exemplo estão em `src/` (`teste1.txt` a `teste4.txt`). Carregue cada um com o botão
**Carregar de Arquivo (.txt)**.

### Exemplo 1 — Quantidade par de zeros (`teste1.txt`)

```
0 1
q0 q1
q0
q0
q0 0 q1
q0 1 q0
q1 0 q0
q1 1 q1
```

| Cadeia | Resultado | Caminho |
|---|---|---|
| _(vazia)_ | ACEITA | `q0` |
| `11` | ACEITA | `q0 -> q0 -> q0` |
| `00` | ACEITA | `q0 -> q1 -> q0` |
| `1001` | ACEITA | `q0 -> q0 -> q1 -> q0 -> q0` |
| `0` | REJEITADA | `q0 -> q1` |
| `10` | REJEITADA | `q0 -> q0 -> q1` |
| `0111` | REJEITADA | `q0 -> q1 -> q1 -> q1 -> q1` |
| `012` | REJEITADA (símbolo `2` fora do alfabeto) | `q0 -> q1 -> q1 -> POÇO` |

A cadeia vazia é aceita porque o estado inicial também é de aceitação.

### Exemplo 2 — Cadeias que terminam em `01` (`teste2.txt`)

```
0 1
q0 q1 q2
q0
q2
q0 0 q1
q0 1 q0
q1 0 q1
q1 1 q2
q2 0 q1
q2 1 q0
```

| Cadeia | Resultado | Caminho |
|---|---|---|
| `01` | ACEITA | `q0 -> q1 -> q2` |
| `1101` | ACEITA | `q0 -> q0 -> q0 -> q1 -> q2` |
| `0001` | ACEITA | `q0 -> q1 -> q1 -> q1 -> q2` |
| `0` | REJEITADA | `q0 -> q1` |
| `10` | REJEITADA | `q0 -> q0 -> q1` |
| `011` | REJEITADA | `q0 -> q1 -> q2 -> q0` |
| `010` | REJEITADA | `q0 -> q1 -> q2 -> q1` |

A cadeia `010` passa por `q2` (aceitação) no meio, mas termina em `q1`; por isso é rejeitada.

### Exemplo 3 — Autômato não determinístico (`teste3.txt`)

```
0 1
q0 q1
q0
q1
q0 0 q0
q0 0 q1
q0 1 q0
q1 0 q1
q1 1 q1
```

Resultado esperado ao carregar: **erro**, pois `q0` possui duas transições com o símbolo `0`:

> Erro ao carregar arquivo: Autômato Não-Determinístico: O estado 'q0' já possui uma transição para
> o símbolo '0' apontando para 'q0'.

O autômato é rejeitado e nenhuma cadeia pode ser testada.

### Exemplo 4 — AFD incompleto (`teste4.txt`)

```
0 1
q0 q1 q2
q0
q2
q0 0 q1
q1 1 q2
```

Este autômato aceita apenas a cadeia `01`. Ao carregar, aparece o aviso de AFD incompleto, listando
`δ(q0, 1), δ(q1, 0), δ(q2, 0), δ(q2, 1)`.

| Cadeia | Resultado | Caminho |
|---|---|---|
| `01` | ACEITA | `q0 -> q1 -> q2` |
| `0` | REJEITADA (estado não final) | `q0 -> q1` |
| `1` | REJEITADA (sem transição) | `q0 -> POÇO` |
| `00` | REJEITADA (sem transição) | `q0 -> q1 -> POÇO` |
| `011` | REJEITADA (sem transição) | `q0 -> q1 -> q2 -> POÇO` |

---

## 7. Estrutura do projeto

```
pom.xml
src/main/java/org/example/
  Main.java                    ponto de entrada (abre a janela)
  JanelaPrincipal.java         interface gráfica (Swing)
  Automato.java                a 5-tupla (Q, Σ, δ, q0, F) e a simulação
  Estado.java                  estado e suas transições (garante o determinismo)
  Transicao.java               δ(origem, símbolo) = destino
  Simulador.java               execução de cadeias
  GerenciadorIO.java           leitura/gravação de arquivos .txt
  AutomatoInvalidoException.java
  ResultadoSimulacao.java, PassoSimulacao.java, StatusSimulacao.java
  MinimizadorAFD.java          minimização de AFD (recurso extra, não exposto na interface)
src/test/java/org/example/     testes automatizados (JUnit 5)
src/automato.txt, teste1.txt … teste4.txt   exemplos
```

## 8. Limitações

- Cada símbolo do alfabeto deve ter **um único caractere**, pois a cadeia é lida caractere a caractere.
- Nomes de estados e símbolos não podem conter espaços.
