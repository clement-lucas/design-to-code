package com.example.proman.unit.service;

import com.example.proman.entity.CodeName;
import com.example.proman.repository.CodeNameRepository;
import com.example.proman.service.CodeNameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeNameService 単体テスト")
class CodeNameServiceTest {

    @Mock
    private CodeNameRepository codeNameRepository;

    @InjectMocks
    private CodeNameService codeNameService;

    private CodeName createCodeName(String codeId, String codeValue, String codeName) {
        CodeName cn = new CodeName();
        cn.setCodeId(codeId);
        cn.setCodeValue(codeValue);
        cn.setCodeName(codeName);
        cn.setLang("ja");
        return cn;
    }

    @Test
    @DisplayName("S16: getCodeList - コードリスト取得")
    void getCodeList_returnsCodeNames() {
        CodeName cn1 = createCodeName("C0300001", "01", "新規開発");
        CodeName cn2 = createCodeName("C0300001", "02", "保守");
        when(codeNameRepository.findByCodeIdJa("C0300001")).thenReturn(List.of(cn1, cn2));

        List<CodeName> result = codeNameService.getCodeList(CodeNameService.PROJECT_TYPE);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("S17: getCodeMap - コードマップ取得")
    void getCodeMap_returnsLinkedHashMap() {
        CodeName cn1 = createCodeName("C0300001", "01", "新規開発");
        CodeName cn2 = createCodeName("C0300001", "02", "保守");
        when(codeNameRepository.findByCodeIdJa("C0300001")).thenReturn(List.of(cn1, cn2));

        Map<String, String> result = codeNameService.getCodeMap(CodeNameService.PROJECT_TYPE);

        assertThat(result).hasSize(2);
        assertThat(result.get("01")).isEqualTo("新規開発");
        assertThat(result.get("02")).isEqualTo("保守");
    }

    @Test
    @DisplayName("S18: getCodeName - 存在するコード名称取得")
    void getCodeName_existing_returnsName() {
        CodeName cn = createCodeName("C0300001", "01", "新規開発");
        when(codeNameRepository.findByCodeIdJa("C0300001")).thenReturn(List.of(cn));

        String result = codeNameService.getCodeName(CodeNameService.PROJECT_TYPE, "01");

        assertThat(result).isEqualTo("新規開発");
    }

    @Test
    @DisplayName("S19: getCodeName - 存在しないコード値は空文字")
    void getCodeName_notExisting_returnsEmpty() {
        when(codeNameRepository.findByCodeIdJa("C0300001")).thenReturn(List.of());

        String result = codeNameService.getCodeName(CodeNameService.PROJECT_TYPE, "99");

        assertThat(result).isEmpty();
    }
}
