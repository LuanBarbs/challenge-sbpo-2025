package org.sbpo2025.challenge.solvers;

import org.apache.commons.lang3.time.StopWatch;
import org.sbpo2025.challenge.ChallengeSolution;

import java.util.*;

// Solução viável mínima:
// 1. Iterar sobre os pedidos até atingir o mínimo waveSizeLB;
// 2. Para cada pedido, encontrar corredores que tenham os itens e quantidades suficientes;
// 3. Incluir o pedido se for possível atendê-lo;
// 4. Parar ao atingir waveSizeUB.

public class BasicSolver {
    public static ChallengeSolution generate (
        List<Map<Integer, Integer>> orders, // Lista de pedidos (mapa de item -> quantidade).
        List<Map<Integer, Integer>> aisles, // Lista de corredores (mapa de item -> quantidade).
        int nItems,                         // Total de tipos de itens.
        int waveSizeLB,                     // Mínimo de unidades na wave.
        int waveSizeUB,                     // Máximo de unidades na wave.
        StopWatch stopWatch)
    {
        // Conjunto de índices dos pedidos selecionados.
        Set<Integer> selectedOrders = new HashSet<>();

        // Conjunto de índices dos corredores utilizados.
        Set<Integer> usedAisles = new HashSet<>();

        // Cria uma cópia dos corredores para modificar quantidades disponíveis.
        List<Map<Integer, Integer>> aislesCopy = new ArrayList<>();
        for (Map<Integer, Integer> aisle : aisles) {
            aislesCopy.add(new HashMap<>(aisle));
        }

        // Unidades totais adicionadas à wave.
        int totalUnitsPicked = 0;

        // Tenta adicionar pedidos até alcançar o máximo waveSizeUB.
        for (int orderIdx = 0; orderIdx < orders.size(); orderIdx++) {
            Map<Integer, Integer> order = orders.get(orderIdx);

            // Soma o total de unidades no pedido.
            int orderUnits = order.values().stream().mapToInt(Integer::intValue).sum();

            // Se adicionar este pedido ultrapassa o limite superior, pula o pedido.
            if (totalUnitsPicked + orderUnits > waveSizeUB) continue;

            boolean canFulfill = true; // Asume inicialmente que o pedido pode ser atendido.

            Map<Integer, Integer> tempAisleUsage = new HashMap<>(); // Simulação de consumos.
            Set<Integer> aislesUsedInThisOrder = new HashSet<>();   // Corredores usados nesse pedido.

            // Verifica se todos os itens do pedido podem ser atendidos por algum corredor.
            for (Map.Entry<Integer, Integer> entry : order.entrySet()) {
                int item = entry.getKey();      // Item necessário.
                int needed = entry.getValue();  // Quantidade necessária.
                boolean found = false;

                // Percorre os corredores em busca do item.
                for (int aisleIdx = 0; aisleIdx < aislesCopy.size(); aisleIdx++) {
                    Map<Integer, Integer> aisle = aislesCopy.get(aisleIdx);
                    int available = aisle.getOrDefault(item, 0);

                    if (available >= needed) {
                        // Salva uso simulado com chave composta (aisleIdx, item)
                        tempAisleUsage.put(aisleIdx * 100000 + item, needed);
                        aislesUsedInThisOrder.add(aisleIdx);
                        found = true;
                        break; // Item encontrado, sai do loop.
                    }
                }

                // Se o item não foi encontrado, pedido não atendido.
                if (!found) {
                    canFulfill = false;
                    break;
                }
            }

            // Se o pedido pode ser atendido, consome os itens dos corredores.
            if (canFulfill) {
                selectedOrders.add(orderIdx);
                totalUnitsPicked += orderUnits;

                // Aplica os consumos nos corredores.
                for (Map.Entry<Integer, Integer> usage : tempAisleUsage.entrySet()) {
                    int key = usage.getKey();
                    int aisleIdx = key / 100000;
                    int item = key % 100000;
                    int quantity = usage.getValue();
                    Map<Integer, Integer> aisle = aislesCopy.get(aisleIdx);
                    aisle.put(item, aisle.get(item) - quantity);
                    usedAisles.add(aisleIdx);
                }

                // if (totalUnitsPicked >= waveSizeLB) break;
            }          
        }

        // Retorna a solução criada com os pedidos selecionados e corredores usados.
        return new ChallengeSolution(selectedOrders, usedAisles);
    }
}