package org.sbpo2025.challenge.solvers;

import org.apache.commons.lang3.time.StopWatch;
import org.sbpo2025.challenge.ChallengeSolution;

import java.util.*;

// Solução Gulosa:
// 1. Ordenar pedidos pela quantidade de unidades (decrescente).
// 2. Selecionar pedidos respeitando limite superior (waveSizeUB).
// 3. Para cada corredor, calcule quantos itens ainda não atendidos ele pode contribuir para os pedidos selecionados.
// 4. Ordene os corredores pela quantidade total que eles podem contribuir.
// 5. Para cada corredor (do mais útil ao menos útil):
//      5.1 Para cada pedido ainda não totalmente atendido, veja se o corredor pode contribuir com algum item.
//      5.2 Subtraia do corredor e adicione ao atendimento do pedido.
//      5.3 Se o pedido ficar completamente atendido, marque como completo.
//      5.4 Adicione o corredor ao conjunto de corredores usados.
// 6. Parar quando todos os pedidos estiverem completamente atendidos.


public class GreedySolver {
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

        // Lista de pares (índice do pedido, total de unidades no pedido).
        List<int[]> orderWithQuantities = new ArrayList<>();

        // Ordenar pedidos pela quantidade de unidades (decrescente).
        for (int i = 0; i < orders.size(); i++) {
            int totalUnits = orders.get(i).values().stream().mapToInt(Integer::intValue).sum();
            orderWithQuantities.add(new int[]{i, totalUnits});
        }
        orderWithQuantities.sort((a, b) -> Integer.compare(b[1], a[1]));

        // Unidades totais adicionadas à wave.
        int totalUnitsPicked = 0;

        // Tenta adicionar pedidos até alcançar o máximo waveSizeUB.
        for (int orderIdx = 0; orderIdx < orderWithQuantities.size(); orderIdx++) {
            int[] orderInfo = orderWithQuantities.get(orderIdx);
            int orderIndex = orderInfo[0];
            int orderUnits = orderInfo[1];

            // Se adicionar este pedido ultrapassa o limite superior, pula o pedido.
            if (totalUnitsPicked + orderUnits > waveSizeUB) continue;

            selectedOrders.add(orderIndex);
            totalUnitsPicked += orderUnits;

            // Se é exatamente o limite superior, adiciona e para.
            if (totalUnitsPicked + orderUnits == waveSizeUB) break;
        }

        // Mapeia a necessidade restante de cada pedido selecionado (item -> quantidade).
        Map<Integer, Map<Integer, Integer>> remainingNeeds = new HashMap<>();
        for (int idx : selectedOrders) {
            // Clona o mapa do pedido para evitar modificar o original.
            remainingNeeds.put(idx, new HashMap<>(orders.get(idx)));
        }

        // Conjunto de corredores utilizados.
        Set<Integer> usedAisles = new HashSet<>();

        // Faz uma cópia mutável dos corredores para acompanhar redução de estoque.
        List<Map<Integer, Integer>> aislesCopy = new ArrayList<>();
        for (Map<Integer, Integer> aisle : aisles) {
            aislesCopy.add(new HashMap<>(aisle));
        }

        // Enquanto houver necessidade em algum pedido.
        while (!remainingNeeds.isEmpty()) {
            int bestAisleIdx = -1;
            int maxContribution = -1;

            // Encontra o corredor que mais pode contribuir com os itens ainda não atendidos.
            for (int i = 0; i < aislesCopy.size(); i++) {
                Map<Integer, Integer> aisle = aislesCopy.get(i);
                int contribution = 0;

                // Soma o quanto esse corredor pode atender da necessidade total.
                for (Map<Integer, Integer> needs : remainingNeeds.values()) {
                    for (Map.Entry<Integer, Integer> itemEntry : needs.entrySet()) {
                        int itemId = itemEntry.getKey();
                        int neededQty = itemEntry.getValue();
                        int availableQty = aisle.getOrDefault(itemId, 0);
                        contribution += Math.min(neededQty, availableQty);
                    }
                }

                // Atualiza se este corredor é o melhor até agora.
                if (contribution > maxContribution) {
                    bestAisleIdx = i;
                    maxContribution = contribution;
                }
            }

            // Se nenhum corredor pode ajudar mais, interrompe (instância impossível de resolver com corredores disponíveis).
            if (maxContribution == 0) break;

            // Usa o melhor corredor encontrado.
            Map<Integer, Integer> bestAisle = aislesCopy.get(bestAisleIdx);
            usedAisles.add(bestAisleIdx);

            // Atualiza as necessidades de cada pedido.
            Iterator<Map.Entry<Integer, Map<Integer, Integer>>> orderIt = remainingNeeds.entrySet().iterator();
            while (orderIt.hasNext()) {
                Map.Entry<Integer, Map<Integer, Integer>> orderEntry = orderIt.next();
                Map<Integer, Integer> needs = orderEntry.getValue();

                Iterator<Map.Entry<Integer, Integer>> itemIt = needs.entrySet().iterator();
                while (itemIt.hasNext()) {
                    Map.Entry<Integer, Integer> itemEntry = itemIt.next();
                    int itemId = itemEntry.getKey();
                    int neededQty = itemEntry.getValue();
                    int availableQty = bestAisle.getOrDefault(itemId, 0);

                    if (availableQty > 0) {
                        int usedQty = Math.min(neededQty, availableQty);

                        // Atualiza o estoque do corredor.
                        bestAisle.put(itemId, availableQty - usedQty);

                        // Atualiza o que falta do pedido
                        if (usedQty == neededQty) {
                            itemIt.remove(); // Item completamente atendido.
                        } else {
                            needs.put(itemId, neededQty - usedQty);
                        }
                    }
                }

                // Se o pedido não precisa mais de nenhum item, remove da lista de necessidades.
                if (needs.isEmpty()) {
                    orderIt.remove();
                }
            }
        }

        // Cria e retorna a solução.
        return new ChallengeSolution(selectedOrders, usedAisles);
    }
}