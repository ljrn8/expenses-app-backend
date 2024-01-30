package com.example.beginnerexpensesappapi.service;

import com.example.beginnerexpensesappapi.PurchasesDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DTOService {

    public HashMap<String, Integer> PurchasestoHash(PurchasesDTO dto) {
        return new HashMap<String, Integer>(Map.of(
                "apples", dto.getApples(),
                "bananas", dto.getBananas(),
                "oranges", dto.getOranges()
        ));
    }

}
