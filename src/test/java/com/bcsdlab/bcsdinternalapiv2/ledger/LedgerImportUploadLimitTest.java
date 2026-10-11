package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * 업로드 크기 한도(1MB)는 실제 서블릿 컨테이너가 multipart를 해석할 때 걸린다. MockMvc로는 재현되지 않아
 * 이 클래스만 실제 포트로 띄워 HTTP로 보낸다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LedgerImportUploadLimitTest extends LedgerIntegrationTestSupport {

    @LocalServerPort
    private int port;

    @Test
    void 파일이_1MB를_넘으면_413_IMPORT_FILE_TOO_LARGE다() throws Exception {
        HttpResponse<String> response = upload(new byte[1024 * 1024 + 1]);

        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(response.body()).contains("거래내역 파일이 너무 큽니다");
    }

    @Test
    void 한도_1MB_이하는_크기_한도에_걸리지_않는다() throws Exception {
        byte[] file = XlsxFixture.bankOriginal().deposit("2026.09.01 10:00:00", 100, "회원", 100).build();

        HttpResponse<String> response = upload(file);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"transactions\"");
    }

    private HttpResponse<String> upload(byte[] file) throws Exception {
        String boundary = "----ledger-test-boundary";
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.xlsx\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(file);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/v1/admin/ledger/imports/preview"))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}
