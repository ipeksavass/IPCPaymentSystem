package com.ipeksavas.restfulapp.domain.model

object ProductCatalog {

    val dummyProducts = listOf(
        // İçecek Departmanı (ID: 101)
        Product(1, "Su", 10.00, 101, "İçecek"),
        Product(2, "Kola", 25.00, 101, "İçecek"),
        Product(3, "Ayran", 15.00, 101, "İçecek"),
        Product(4, "Meyve Suyu", 30.00, 101, "İçecek"),
        Product(5, "Soğuk Çay", 35.00, 101, "İçecek"),

        // Gıda Departmanı (ID: 102)
        Product(6, "Makarna", 35.00, 102, "Gıda"),
        Product(7, "Peynir", 120.00, 102, "Gıda"),
        Product(8, "Ekmek", 10.00, 102, "Gıda"),
        Product(9, "Zeytin", 85.00, 102, "Gıda"),
        Product(10, "Yumurta (15'li)", 65.00, 102, "Gıda"),

        // Kıyafet Departmanı (ID: 103)
        Product(11, "Tişört", 250.00, 103, "Kıyafet"),
        Product(12, "Pantolon", 400.00, 103, "Kıyafet"),
        Product(13, "Çorap (3'lü)", 60.00, 103, "Kıyafet"),
        Product(14, "Gömlek", 350.00, 103, "Kıyafet"),
        Product(15, "Şapka", 150.00, 103, "Kıyafet"),

        // Temizlik Departmanı (ID: 104)
        Product(16, "Çamaşır Deterjanı", 180.00, 104, "Temizlik"),
        Product(17, "Sıvı Sabun", 45.00, 104, "Temizlik"),
        Product(18, "Çamaşır Suyu", 60.00, 104, "Temizlik"),
        Product(19, "Bulaşık Süngeri", 25.00, 104, "Temizlik"),
        Product(20, "Yüzey Temizleyici", 75.00, 104, "Temizlik")
    ).sortedBy { it.departmentName }
}