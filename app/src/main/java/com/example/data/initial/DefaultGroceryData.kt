package com.example.data.initial

import com.example.data.local.entity.GroceryItemEntity

object DefaultGroceryData {
    val initialItems: List<GroceryItemEntity> = listOf(
        // 🥬 Vegetables
        GroceryItemEntity(
            name = "Onion",
            nativeName = "Vengayam / Pyaz",
            category = "Vegetables",
            iconKey = "onion",
            pricePerUnit = 40.0,
            unit = "kg",
            monthlyQtySingle = "1.5 kg",
            monthlyQtySmall = "4 kg",
            monthlyQtyFamily = "8 kg"
        ),
        GroceryItemEntity(
            name = "Tomato",
            nativeName = "Thakkali / Tamatar",
            category = "Vegetables",
            iconKey = "tomato",
            pricePerUnit = 30.0,
            unit = "kg",
            monthlyQtySingle = "1.5 kg",
            monthlyQtySmall = "4 kg",
            monthlyQtyFamily = "7 kg"
        ),
        GroceryItemEntity(
            name = "Potato",
            nativeName = "Urulaikizhangu / Aloo",
            category = "Vegetables",
            iconKey = "potato",
            pricePerUnit = 35.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "3 kg",
            monthlyQtyFamily = "5 kg"
        ),
        GroceryItemEntity(
            name = "Garlic",
            nativeName = "Poondu / Lahsun",
            category = "Vegetables",
            iconKey = "garlic",
            pricePerUnit = 180.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Ginger",
            nativeName = "Inji / Adrak",
            category = "Vegetables",
            iconKey = "ginger",
            pricePerUnit = 120.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Green Chillies",
            nativeName = "Pachai Milagai / Hari Mirch",
            category = "Vegetables",
            iconKey = "green_chilli",
            pricePerUnit = 60.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Carrots",
            nativeName = "Carrot / Gajar",
            category = "Vegetables",
            iconKey = "carrot",
            pricePerUnit = 50.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Beans",
            nativeName = "Beans / French Beans",
            category = "Vegetables",
            iconKey = "beans",
            pricePerUnit = 60.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Curry Leaves",
            nativeName = "Karuveppilai / Kadi Patta",
            category = "Vegetables",
            iconKey = "curry_leaves",
            pricePerUnit = 10.0,
            unit = "bunch",
            monthlyQtySingle = "4 bunches",
            monthlyQtySmall = "8 bunches",
            monthlyQtyFamily = "12 bunches"
        ),
        GroceryItemEntity(
            name = "Coriander Leaves",
            nativeName = "Kothamalli / Dhaniya Patta",
            category = "Vegetables",
            iconKey = "coriander",
            pricePerUnit = 15.0,
            unit = "bunch",
            monthlyQtySingle = "4 bunches",
            monthlyQtySmall = "8 bunches",
            monthlyQtyFamily = "12 bunches"
        ),
        GroceryItemEntity(
            name = "Mint Leaves",
            nativeName = "Pudhina / Pudina",
            category = "Vegetables",
            iconKey = "mint",
            pricePerUnit = 15.0,
            unit = "bunch",
            monthlyQtySingle = "2 bunches",
            monthlyQtySmall = "4 bunches",
            monthlyQtyFamily = "6 bunches"
        ),
        GroceryItemEntity(
            name = "Bottle Gourd",
            nativeName = "Suraikkai / Lauki",
            category = "Vegetables",
            iconKey = "bottle_gourd",
            pricePerUnit = 30.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Eggplant (Brinjal)",
            nativeName = "Kathirikkai / Baingan",
            category = "Vegetables",
            iconKey = "brinjal",
            pricePerUnit = 40.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2.5 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Okra (Bhindi)",
            nativeName = "Vendaikkai / Lady's Finger",
            category = "Vegetables",
            iconKey = "bhindi",
            pricePerUnit = 45.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Spinach (Palak)",
            nativeName = "Keerai / Palak",
            category = "Vegetables",
            iconKey = "spinach",
            pricePerUnit = 25.0,
            unit = "bunch",
            monthlyQtySingle = "4 bunches",
            monthlyQtySmall = "8 bunches",
            monthlyQtyFamily = "12 bunches"
        ),
        GroceryItemEntity(
            name = "Bitter Gourd",
            nativeName = "Pavakkai / Karela",
            category = "Vegetables",
            iconKey = "bitter_gourd",
            pricePerUnit = 50.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),

        // 🍎 Fruits
        GroceryItemEntity(
            name = "Apples",
            nativeName = "Apple / Seb",
            category = "Fruits",
            iconKey = "apple",
            pricePerUnit = 160.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "3 kg",
            monthlyQtyFamily = "5 kg"
        ),
        GroceryItemEntity(
            name = "Bananas",
            nativeName = "Vazhaipazham / Kela",
            category = "Fruits",
            iconKey = "banana",
            pricePerUnit = 50.0,
            unit = "kg",
            monthlyQtySingle = "2 kg",
            monthlyQtySmall = "4 kg",
            monthlyQtyFamily = "8 kg"
        ),
        GroceryItemEntity(
            name = "Grapes",
            nativeName = "Thiratchai / Angoor",
            category = "Fruits",
            iconKey = "grape",
            pricePerUnit = 90.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Oranges",
            nativeName = "Kamala Orange / Santra",
            category = "Fruits",
            iconKey = "orange",
            pricePerUnit = 80.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Lemons",
            nativeName = "Elumichai / Nimbu",
            category = "Fruits",
            iconKey = "lemon",
            pricePerUnit = 6.0,
            unit = "pc",
            monthlyQtySingle = "8 pcs",
            monthlyQtySmall = "16 pcs",
            monthlyQtyFamily = "30 pcs"
        ),
        GroceryItemEntity(
            name = "Mango",
            nativeName = "Mambazham / Aam",
            category = "Fruits",
            iconKey = "mango",
            pricePerUnit = 95.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "3 kg",
            monthlyQtyFamily = "5 kg"
        ),
        GroceryItemEntity(
            name = "Pomegranate",
            nativeName = "Madulai / Anar",
            category = "Fruits",
            iconKey = "pomegranate",
            pricePerUnit = 150.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Papaya",
            nativeName = "Pappali / Papita",
            category = "Fruits",
            iconKey = "papaya",
            pricePerUnit = 40.0,
            unit = "kg",
            monthlyQtySingle = "1 pc",
            monthlyQtySmall = "2 pcs",
            monthlyQtyFamily = "4 pcs"
        ),
        GroceryItemEntity(
            name = "Kiwi",
            nativeName = "Kiwi Fruit",
            category = "Fruits",
            iconKey = "kiwi",
            pricePerUnit = 35.0,
            unit = "pc",
            monthlyQtySingle = "3 pcs",
            monthlyQtySmall = "6 pcs",
            monthlyQtyFamily = "10 pcs"
        ),

        // 🌾 Spices & Condiments
        GroceryItemEntity(
            name = "Turmeric Powder",
            nativeName = "Manjal Thool / Haldi",
            category = "Spices & Condiments",
            iconKey = "turmeric",
            pricePerUnit = 180.0,
            unit = "kg",
            monthlyQtySingle = "100 g",
            monthlyQtySmall = "250 g",
            monthlyQtyFamily = "500 g"
        ),
        GroceryItemEntity(
            name = "Red Chili Powder",
            nativeName = "Milagai Thool / Lal Mirch",
            category = "Spices & Condiments",
            iconKey = "chili_powder",
            pricePerUnit = 240.0,
            unit = "kg",
            monthlyQtySingle = "200 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Coriander Powder",
            nativeName = "Malli Thool / Dhaniya Powder",
            category = "Spices & Condiments",
            iconKey = "coriander_powder",
            pricePerUnit = 200.0,
            unit = "kg",
            monthlyQtySingle = "200 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Cumin Seeds (Jeera)",
            nativeName = "Jeeragam / Cumin",
            category = "Spices & Condiments",
            iconKey = "jeera",
            pricePerUnit = 360.0,
            unit = "kg",
            monthlyQtySingle = "100 g",
            monthlyQtySmall = "250 g",
            monthlyQtyFamily = "500 g"
        ),
        GroceryItemEntity(
            name = "Mustard Seeds (Rai)",
            nativeName = "Kadugu / Sarson",
            category = "Spices & Condiments",
            iconKey = "mustard_seeds",
            pricePerUnit = 120.0,
            unit = "kg",
            monthlyQtySingle = "100 g",
            monthlyQtySmall = "250 g",
            monthlyQtyFamily = "500 g"
        ),
        GroceryItemEntity(
            name = "Garam Masala",
            nativeName = "Garam Masala Powder",
            category = "Spices & Condiments",
            iconKey = "garam_masala",
            pricePerUnit = 380.0,
            unit = "kg",
            monthlyQtySingle = "100 g",
            monthlyQtySmall = "200 g",
            monthlyQtyFamily = "400 g"
        ),
        GroceryItemEntity(
            name = "Asafoetida (Hing)",
            nativeName = "Perungayam / Hing",
            category = "Spices & Condiments",
            iconKey = "hing",
            pricePerUnit = 65.0,
            unit = "pack",
            monthlyQtySingle = "1 pack",
            monthlyQtySmall = "1 pack",
            monthlyQtyFamily = "2 packs"
        ),
        GroceryItemEntity(
            name = "Cardamom (Elaichi)",
            nativeName = "Elakkai / Green Cardamom",
            category = "Spices & Condiments",
            iconKey = "cardamom",
            pricePerUnit = 2600.0,
            unit = "kg",
            monthlyQtySingle = "25 g",
            monthlyQtySmall = "50 g",
            monthlyQtyFamily = "100 g"
        ),
        GroceryItemEntity(
            name = "Cloves (Laung)",
            nativeName = "Krambu / Lavang",
            category = "Spices & Condiments",
            iconKey = "cloves",
            pricePerUnit = 1200.0,
            unit = "kg",
            monthlyQtySingle = "25 g",
            monthlyQtySmall = "50 g",
            monthlyQtyFamily = "100 g"
        ),
        GroceryItemEntity(
            name = "Fenugreek Seeds (Methi)",
            nativeName = "Vendhayam / Methi Dana",
            category = "Spices & Condiments",
            iconKey = "fenugreek",
            pricePerUnit = 110.0,
            unit = "kg",
            monthlyQtySingle = "100 g",
            monthlyQtySmall = "250 g",
            monthlyQtyFamily = "500 g"
        ),

        // 🌾 Lentils & Pulses
        GroceryItemEntity(
            name = "Pigeon Pea (Toor Dal)",
            nativeName = "Tuvaram Paruppu / Arhar Dal",
            category = "Lentils & Pulses",
            iconKey = "toor_dal",
            pricePerUnit = 165.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Black Gram (Urad Dal)",
            nativeName = "Ulundham Paruppu / Idli Dal",
            category = "Lentils & Pulses",
            iconKey = "urad_dal",
            pricePerUnit = 145.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Green Gram (Moong Dal)",
            nativeName = "Pasi Paruppu / Moong",
            category = "Lentils & Pulses",
            iconKey = "moong_dal",
            pricePerUnit = 130.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "2.5 kg"
        ),
        GroceryItemEntity(
            name = "Split Bengal Gram (Chana Dal)",
            nativeName = "Kadalai Paruppu / Chana",
            category = "Lentils & Pulses",
            iconKey = "chana_dal",
            pricePerUnit = 105.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Red Lentils (Masoor Dal)",
            nativeName = "Masoor Dal / Mysore Paruppu",
            category = "Lentils & Pulses",
            iconKey = "masoor_dal",
            pricePerUnit = 110.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Kidney Beans (Rajma)",
            nativeName = "Rajma / Red Beans",
            category = "Lentils & Pulses",
            iconKey = "rajma",
            pricePerUnit = 150.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Chickpeas (Kabuli Chana)",
            nativeName = "Vellai Kondakadalai / White Chana",
            category = "Lentils & Pulses",
            iconKey = "kabuli_chana",
            pricePerUnit = 140.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),

        // 🌾 Flours & Grains
        GroceryItemEntity(
            name = "Wheat Flour (Atta)",
            nativeName = "Kothumai Maavu / Gehun Atta",
            category = "Flours & Grains",
            iconKey = "atta",
            pricePerUnit = 45.0,
            unit = "kg",
            monthlyQtySingle = "2 kg",
            monthlyQtySmall = "5 kg",
            monthlyQtyFamily = "10 kg"
        ),
        GroceryItemEntity(
            name = "Rice (Sona Masoori / Ponni)",
            nativeName = "Ponni Arisi / Daily Cooking Rice",
            category = "Flours & Grains",
            iconKey = "rice",
            pricePerUnit = 60.0,
            unit = "kg",
            monthlyQtySingle = "5 kg",
            monthlyQtySmall = "12 kg",
            monthlyQtyFamily = "25 kg"
        ),
        GroceryItemEntity(
            name = "Basmati Rice",
            nativeName = "Basmati Arisi / Biryani Rice",
            category = "Flours & Grains",
            iconKey = "basmati",
            pricePerUnit = 125.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "5 kg"
        ),
        GroceryItemEntity(
            name = "Rice Flour",
            nativeName = "Arisi Maavu / Chawal Atta",
            category = "Flours & Grains",
            iconKey = "rice_flour",
            pricePerUnit = 48.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Besan (Gram Flour)",
            nativeName = "Kadalai Maavu / Chana Atta",
            category = "Flours & Grains",
            iconKey = "besan",
            pricePerUnit = 95.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Poha (Flattened Rice)",
            nativeName = "Aval / Chivda",
            category = "Flours & Grains",
            iconKey = "poha",
            pricePerUnit = 55.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Semolina (Rava / Sooji)",
            nativeName = "Rava / Sooji",
            category = "Flours & Grains",
            iconKey = "rava",
            pricePerUnit = 52.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1.5 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Vermicelli (Semiya)",
            nativeName = "Semiya / Seviyan",
            category = "Flours & Grains",
            iconKey = "vermicelli",
            pricePerUnit = 30.0,
            unit = "pack",
            monthlyQtySingle = "2 packs",
            monthlyQtySmall = "4 packs",
            monthlyQtyFamily = "8 packs"
        ),

        // 🌾 Oils & Ghee
        GroceryItemEntity(
            name = "Sunflower Cooking Oil",
            nativeName = "Gold Winner / Sunflower Ennai",
            category = "Oils & Ghee",
            iconKey = "oil",
            pricePerUnit = 135.0,
            unit = "L",
            monthlyQtySingle = "1 L",
            monthlyQtySmall = "3 L",
            monthlyQtyFamily = "5 L"
        ),
        GroceryItemEntity(
            name = "Groundnut Oil",
            nativeName = "Kadalai Ennai / Mungfali Tel",
            category = "Oils & Ghee",
            iconKey = "groundnut_oil",
            pricePerUnit = 190.0,
            unit = "L",
            monthlyQtySingle = "1 L",
            monthlyQtySmall = "2 L",
            monthlyQtyFamily = "3 L"
        ),
        GroceryItemEntity(
            name = "Coconut Oil",
            nativeName = "Thengai Ennai / Nariyal Tel",
            category = "Oils & Ghee",
            iconKey = "coconut_oil",
            pricePerUnit = 240.0,
            unit = "L",
            monthlyQtySingle = "500 ml",
            monthlyQtySmall = "1 L",
            monthlyQtyFamily = "2 L"
        ),
        GroceryItemEntity(
            name = "Mustard Oil",
            nativeName = "Kadugu Ennai / Sarson Tel",
            category = "Oils & Ghee",
            iconKey = "mustard_oil",
            pricePerUnit = 155.0,
            unit = "L",
            monthlyQtySingle = "500 ml",
            monthlyQtySmall = "1 L",
            monthlyQtyFamily = "2 L"
        ),
        GroceryItemEntity(
            name = "Pure Ghee",
            nativeName = "Pasu Nei / Clarified Butter",
            category = "Oils & Ghee",
            iconKey = "ghee",
            pricePerUnit = 580.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),

        // 🌾 Basic Condiments & Staples
        GroceryItemEntity(
            name = "Sugar",
            nativeName = "Chakkarai / Seeni",
            category = "Staples & Condiments",
            iconKey = "sugar",
            pricePerUnit = 44.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2.5 kg",
            monthlyQtyFamily = "5 kg"
        ),
        GroceryItemEntity(
            name = "Cooking Salt",
            nativeName = "Thool Uppu / Table Salt",
            category = "Staples & Condiments",
            iconKey = "salt",
            pricePerUnit = 22.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Crystal Salt",
            nativeName = "Kal Uppu / Rock Salt",
            category = "Staples & Condiments",
            iconKey = "crystal_salt",
            pricePerUnit = 16.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "3 kg"
        ),
        GroceryItemEntity(
            name = "Jaggery (Gur)",
            nativeName = "Vellam / Nattu Sakkarai",
            category = "Staples & Condiments",
            iconKey = "jaggery",
            pricePerUnit = 75.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Tamarind (Puli / Imli)",
            nativeName = "Puli / Sour Tamarind",
            category = "Staples & Condiments",
            iconKey = "tamarind",
            pricePerUnit = 140.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Fresh Coconut",
            nativeName = "Thengai / Nariyal",
            category = "Staples & Condiments",
            iconKey = "coconut",
            pricePerUnit = 32.0,
            unit = "pc",
            monthlyQtySingle = "4 pcs",
            monthlyQtySmall = "8 pcs",
            monthlyQtyFamily = "15 pcs"
        ),
        GroceryItemEntity(
            name = "Bay Leaves (Tej Patta)",
            nativeName = "Biryani Ilai / Bay Leaf",
            category = "Staples & Condiments",
            iconKey = "bay_leaves",
            pricePerUnit = 35.0,
            unit = "pack",
            monthlyQtySingle = "1 pack",
            monthlyQtySmall = "1 pack",
            monthlyQtyFamily = "2 packs"
        ),

        // 🌾 Beverages
        GroceryItemEntity(
            name = "Tea Powder (Chai)",
            nativeName = "Tea Thool / Tea Dust",
            category = "Beverages",
            iconKey = "tea",
            pricePerUnit = 320.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Coffee Powder",
            nativeName = "Kapi Thool / Filter Coffee",
            category = "Beverages",
            iconKey = "coffee",
            pricePerUnit = 460.0,
            unit = "kg",
            monthlyQtySingle = "200 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Buttermilk (Chaas)",
            nativeName = "Moor / Spiced Chaas",
            category = "Beverages",
            iconKey = "chaas",
            pricePerUnit = 20.0,
            unit = "pack",
            monthlyQtySingle = "4 packs",
            monthlyQtySmall = "10 packs",
            monthlyQtyFamily = "20 packs"
        ),

        // 🌾 Snacks & Sweets
        GroceryItemEntity(
            name = "Namkeen / Mixture",
            nativeName = "Kara Mixture / Sev",
            category = "Snacks & Sweets",
            iconKey = "namkeen",
            pricePerUnit = 190.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Papad / Appalam",
            nativeName = "Appalam / Papad",
            category = "Snacks & Sweets",
            iconKey = "papad",
            pricePerUnit = 42.0,
            unit = "pack",
            monthlyQtySingle = "2 packs",
            monthlyQtySmall = "4 packs",
            monthlyQtyFamily = "8 packs"
        ),
        GroceryItemEntity(
            name = "Ladoo",
            nativeName = "Boondi / Motichoor Laddu",
            category = "Snacks & Sweets",
            iconKey = "ladoo",
            pricePerUnit = 240.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),
        GroceryItemEntity(
            name = "Gulab Jamun",
            nativeName = "Gulab Jamun Sweets",
            category = "Snacks & Sweets",
            iconKey = "gulab_jamun",
            pricePerUnit = 260.0,
            unit = "kg",
            monthlyQtySingle = "250 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),

        // 🧼 Dairy Products
        GroceryItemEntity(
            name = "Fresh Milk",
            nativeName = "Pasum Paal / Cow Milk",
            category = "Dairy Products",
            iconKey = "milk",
            pricePerUnit = 32.0, // 500ml packet standard
            unit = "pack",
            monthlyQtySingle = "30 packs",
            monthlyQtySmall = "60 packs",
            monthlyQtyFamily = "90 packs"
        ),
        GroceryItemEntity(
            name = "Curd / Yogurt (Dahi)",
            nativeName = "Thayir / Fresh Dahi",
            category = "Dairy Products",
            iconKey = "curd",
            pricePerUnit = 35.0,
            unit = "pack",
            monthlyQtySingle = "15 packs",
            monthlyQtySmall = "30 packs",
            monthlyQtyFamily = "45 packs"
        ),
        GroceryItemEntity(
            name = "Paneer (Cottage Cheese)",
            nativeName = "Fresh Paneer Blocks",
            category = "Dairy Products",
            iconKey = "paneer",
            pricePerUnit = 380.0,
            unit = "kg",
            monthlyQtySingle = "500 g",
            monthlyQtySmall = "1 kg",
            monthlyQtyFamily = "2 kg"
        ),
        GroceryItemEntity(
            name = "Butter",
            nativeName = "Vennai / Makkhan",
            category = "Dairy Products",
            iconKey = "butter",
            pricePerUnit = 520.0,
            unit = "kg",
            monthlyQtySingle = "200 g",
            monthlyQtySmall = "500 g",
            monthlyQtyFamily = "1 kg"
        ),

        // 🧼 Household & Care
        GroceryItemEntity(
            name = "Dishwashing Bar / Gel",
            nativeName = "Vim / Pril Dish Wash",
            category = "Household & Care",
            iconKey = "dishwash",
            pricePerUnit = 35.0,
            unit = "pack",
            monthlyQtySingle = "1 pack",
            monthlyQtySmall = "2 packs",
            monthlyQtyFamily = "4 packs"
        ),
        GroceryItemEntity(
            name = "Laundry Detergent Powder",
            nativeName = "Surf / Ariel Washing Powder",
            category = "Household & Care",
            iconKey = "detergent",
            pricePerUnit = 95.0,
            unit = "kg",
            monthlyQtySingle = "1 kg",
            monthlyQtySmall = "2 kg",
            monthlyQtyFamily = "4 kg"
        ),
        GroceryItemEntity(
            name = "Floor Cleaner Liquid",
            nativeName = "Lizol / Pine Floor Wash",
            category = "Household & Care",
            iconKey = "floor_cleaner",
            pricePerUnit = 115.0,
            unit = "pack",
            monthlyQtySingle = "1 bottle",
            monthlyQtySmall = "1 bottle",
            monthlyQtyFamily = "2 bottles"
        ),
        GroceryItemEntity(
            name = "Toilet Cleaner",
            nativeName = "Harpic / Sanifresh",
            category = "Household & Care",
            iconKey = "toilet_cleaner",
            pricePerUnit = 95.0,
            unit = "pack",
            monthlyQtySingle = "1 bottle",
            monthlyQtySmall = "1 bottle",
            monthlyQtyFamily = "2 bottles"
        ),
        GroceryItemEntity(
            name = "Handwash Liquid",
            nativeName = "Dettol / Lifebuoy Handwash",
            category = "Household & Care",
            iconKey = "handwash",
            pricePerUnit = 75.0,
            unit = "pack",
            monthlyQtySingle = "1 bottle",
            monthlyQtySmall = "1 bottle",
            monthlyQtyFamily = "2 bottles"
        ),
        GroceryItemEntity(
            name = "Bathing Soap",
            nativeName = "Hamam / Mysore Sandal Soap",
            category = "Household & Care",
            iconKey = "soap",
            pricePerUnit = 38.0,
            unit = "pc",
            monthlyQtySingle = "2 pcs",
            monthlyQtySmall = "4 pcs",
            monthlyQtyFamily = "8 pcs"
        ),
        GroceryItemEntity(
            name = "Toothpaste",
            nativeName = "Colgate / CloseUp Paste",
            category = "Household & Care",
            iconKey = "toothpaste",
            pricePerUnit = 65.0,
            unit = "pack",
            monthlyQtySingle = "1 tube",
            monthlyQtySmall = "2 tubes",
            monthlyQtyFamily = "3 tubes"
        ),
        GroceryItemEntity(
            name = "Shampoo Bottle",
            nativeName = "Clinic Plus / Meera Shampoo",
            category = "Household & Care",
            iconKey = "shampoo",
            pricePerUnit = 95.0,
            unit = "pack",
            monthlyQtySingle = "1 bottle",
            monthlyQtySmall = "1 bottle",
            monthlyQtyFamily = "2 bottles"
        )
    )
}
