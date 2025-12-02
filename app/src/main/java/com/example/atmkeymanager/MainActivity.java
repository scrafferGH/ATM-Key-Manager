package com.example.atmkeymanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private EditText searchInput;
    private TextView resultText;
    private Button searchButton, loadButton;
    private HashMap<String, String> atmData = new HashMap<>();
    private static final int FILE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим все элементы интерфейса
        searchInput = findViewById(R.id.searchInput);
        resultText = findViewById(R.id.resultText);
        searchButton = findViewById(R.id.searchButton);
        loadButton = findViewById(R.id.loadButton);

        // Добавляем тестовые данные для проверки
        loadTestData();

        // Обработчик кнопки загрузки файла
        loadButton.setOnClickListener(v -> openFilePicker());

        // Обработчик кнопки поиска
        searchButton.setOnClickListener(v -> performSearch());
    }

    // Загружаем тестовые данные (3 примера)
    private void loadTestData() {
        atmData.put("ATM0011", "ATM0011 - 26DF 7A9D 947C 832F 4645 5291 CBEF 6D52 / 80CFB2 / 16020");
        atmData.put("ATM0074", "ATM0074 - 9E15 20CB 586D 7904 8F3E 7683 2C4A EC75 / C802E1 / 16082");
        atmData.put("ATM0258", "ATM0258 - F1F8 6BFE BFCE 2CFB 073E 949E AE4A 9743 / 15DFC5 / 16277");
        Toast.makeText(this, "Тестовые данные загружены (3 записи)", Toast.LENGTH_SHORT).show();
    }

    // Открываем выбор файла
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    // Выполняем поиск
    private void performSearch() {
        String input = searchInput.getText().toString().trim();
        
        if (input.isEmpty()) {
            Toast.makeText(this, "Введите номер ATM", Toast.LENGTH_SHORT).show();
            return;
        }

        String result = findATM(input);
        resultText.setText(result);
    }

    // Ищем ATM в загруженных данных
    private String findATM(String number) {
        // Пробуем разные варианты написания
        String[] formats = {
            "ATM" + String.format("%04d", tryParseInt(number)),
            "ATM" + number,
            "АТМ" + String.format("%04d", tryParseInt(number)),
            "АТМ" + number
        };

        for (String format : formats) {
            if (atmData.containsKey(format)) {
                return atmData.get(format);
            }
        }

        return "❌ ATM " + number + " не найден\n\nПопробуйте:\n1. Загрузить файл 1.txt\n2. Проверить правильность номера\n3. Примеры: 11, 74, 258";
    }

    // Пытаемся преобразовать строку в число
    private int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Обрабатываем результат выбора файла
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                readFile(uri);
            }
        }
    }

    // Читаем выбранный файл
    private void readFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            int count = 0;
            atmData.clear();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Ищем строки начинающиеся с ATM или АТМ
                    if (line.startsWith("ATM") || line.startsWith("АТМ")) {
                        String[] parts = line.split(" - ");
                        if (parts.length > 0) {
                            String key = parts[0].trim();
                            atmData.put(key, line);
                            count++;
                        }
                    }
                }
            }
            
            reader.close();
            Toast.makeText(this, "✅ Загружено " + count + " записей из файла", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Ошибка чтения файла", Toast.LENGTH_SHORT).show();
            loadTestData(); // Возвращаем тестовые данные
        }
    }
}
