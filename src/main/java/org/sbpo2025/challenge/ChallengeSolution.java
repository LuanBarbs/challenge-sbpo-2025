package org.sbpo2025.challenge;

import java.util.Set;

// Exemplo de Solução:
// new ChallengeSolution(Set.of(0, 1, 2), Set.of(5, 9));
// Significa que:
//      A wave terá os pedidos de índice 0, 1 e 2;
//      Ela visitará os corredores de índice 5 e 9. 

public record ChallengeSolution(Set<Integer> orders, Set<Integer> aisles) {
}
