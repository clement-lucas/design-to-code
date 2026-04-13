package com.example.proman.service;

import com.example.proman.entity.CodeName;
import com.example.proman.repository.CodeNameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CodeNameService {

    public static final String PROJECT_TYPE = "C0300001";
    public static final String PROJECT_CLASS = "C0200001";
    public static final String INDUSTRY_CLASS = "C0100001";
    public static final String REQUEST_STATUS = "C0400001";

    private final CodeNameRepository codeNameRepository;

    public CodeNameService(CodeNameRepository codeNameRepository) {
        this.codeNameRepository = codeNameRepository;
    }

    public List<CodeName> getCodeList(String codeId) {
        return codeNameRepository.findByCodeIdJa(codeId);
    }

    public Map<String, String> getCodeMap(String codeId) {
        Map<String, String> map = new LinkedHashMap<>();
        for (CodeName cn : getCodeList(codeId)) {
            map.put(cn.getCodeValue(), cn.getCodeName());
        }
        return map;
    }

    public String getCodeName(String codeId, String codeValue) {
        return getCodeMap(codeId).getOrDefault(codeValue, "");
    }
}
