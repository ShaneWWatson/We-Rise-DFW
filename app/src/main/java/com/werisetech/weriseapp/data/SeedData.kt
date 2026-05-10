package com.werisetech.weriseapp.data

/**
 * Bundled starter list of DFW-area services. Lat/lon are approximate centers
 * of the listed addresses. Hours/phones may drift over time — this list is meant
 * to be edited as you confirm details. The ID is stable so updates won't duplicate.
 *
 * Hours format: see [com.werisetech.weriseapp.util.HoursParser].
 */
object SeedData {

    fun all(): List<Service> = listOf(

        // ---------------- FOOD ----------------
        Service(
            id = "food_north_texas_food_bank",
            name = "North Texas Food Bank",
            category = Category.FOOD,
            address = "3677 E Rosemeade Pkwy, Plano, TX 75007",
            phone = "(214) 330-1396",
            hours = "MON 08:00-17:00; TUE 08:00-17:00; WED 08:00-17:00; THU 08:00-17:00; FRI 08:00-17:00",
            latitude = 32.9963,
            longitude = -96.8460,
            faithBased = false,
            blurb = "Regional food bank distributing groceries and prepared meals across 13 counties. Visit website for the closest pantry partner.",
            website = "https://ntfb.org"
        ),
        Service(
            id = "food_tarrant_area_food_bank",
            name = "Tarrant Area Food Bank",
            category = Category.FOOD,
            address = "2525 Cullen St, Fort Worth, TX 76107",
            phone = "(817) 857-7100",
            hours = "MON 09:00-17:00; TUE 09:00-17:00; WED 09:00-17:00; THU 09:00-17:00; FRI 09:00-17:00",
            latitude = 32.7506,
            longitude = -97.3712,
            faithBased = false,
            blurb = "Hunger-relief organization serving Tarrant and 12 surrounding counties. Mobile pantries operate weekly.",
            website = "https://tafb.org"
        ),
        Service(
            id = "food_crossroads_community_services",
            name = "CrossRoads Community Services",
            category = Category.FOOD,
            address = "11540 N Central Expy Suite 100, Dallas, TX 75243",
            phone = "(214) 560-2511",
            hours = "MON 09:00-15:00; TUE 09:00-15:00; WED 09:00-15:00; THU 09:00-15:00",
            latitude = 32.9196,
            longitude = -96.7699,
            faithBased = true,
            blurb = "Food pantry serving low-income households across Dallas. Faith-based; open to all regardless of religion.",
            website = "https://ccsdallas.org"
        ),
        Service(
            id = "food_minnies_food_pantry",
            name = "Minnie's Food Pantry",
            category = Category.FOOD,
            address = "661 18th St, Plano, TX 75074",
            phone = "(972) 596-0253",
            hours = "MON 10:00-15:00; WED 10:00-15:00; FRI 10:00-15:00",
            latitude = 33.0246,
            longitude = -96.6911,
            faithBased = false,
            blurb = "Free groceries for North Texas families. Distributes ~10,000 meals a week.",
            website = "https://minniesfoodpantry.org"
        ),
        Service(
            id = "food_sharing_life",
            name = "Sharing Life Community Outreach",
            category = Category.FOOD,
            address = "3544 E Emporium Cir, Mesquite, TX 75150",
            phone = "(972) 285-5262",
            hours = "MON 09:00-16:00; TUE 09:00-16:00; WED 09:00-16:00; THU 09:00-16:00",
            latitude = 32.8126,
            longitude = -96.6155,
            faithBased = false,
            blurb = "Free food, clothing, and basic services for southeast Dallas County residents.",
            website = "https://sharinglife.org"
        ),
        Service(
            id = "food_community_storehouse",
            name = "Community Storehouse",
            category = Category.FOOD,
            address = "201 N Kimball Ave, Keller, TX 76248",
            phone = "(817) 431-3340",
            hours = "MON 10:00-15:00; TUE 10:00-15:00; WED 10:00-15:00; THU 10:00-15:00",
            latitude = 32.9355,
            longitude = -97.2519,
            faithBased = false,
            blurb = "Northeast Tarrant County food, clothing, and after-school assistance.",
            website = "https://thecommunitystorehouse.org"
        ),
        Service(
            id = "food_christs_haven_pantry",
            name = "Christ's Haven Food Pantry",
            category = Category.FOOD,
            address = "4601 Sycamore School Rd, Fort Worth, TX 76133",
            phone = "(817) 423-1770",
            hours = "TUE 10:00-14:00; THU 10:00-14:00",
            latitude = 32.6422,
            longitude = -97.3870,
            faithBased = true,
            blurb = "Christian-affiliated pantry providing groceries to families in southwest Fort Worth.",
            website = "https://christshaven.org"
        ),

        // -------------- CLOTHING --------------
        Service(
            id = "cloth_genesis_thrift",
            name = "Genesis Benefit Thrift Store",
            category = Category.CLOTHING,
            address = "12200 Coit Rd Suite 100, Dallas, TX 75251",
            phone = "(972) 437-1133",
            hours = "MON 10:00-18:00; TUE 10:00-18:00; WED 10:00-18:00; THU 10:00-18:00; FRI 10:00-18:00; SAT 10:00-18:00",
            latitude = 32.9213,
            longitude = -96.7705,
            faithBased = false,
            blurb = "Affordable clothing; proceeds support Genesis Women's Shelter for survivors of domestic violence.",
            website = "https://genesisshelter.org"
        ),
        Service(
            id = "cloth_attitudes_attire",
            name = "Attitudes & Attire",
            category = Category.CLOTHING,
            address = "2050 Stemmons Fwy Suite 8000, Dallas, TX 75207",
            phone = "(214) 630-0980",
            hours = "MON 09:00-17:00; TUE 09:00-17:00; WED 09:00-17:00; THU 09:00-17:00",
            latitude = 32.8019,
            longitude = -96.8225,
            faithBased = false,
            blurb = "Free professional and interview clothing for women re-entering the workforce. Referral required.",
            website = "https://attitudesandattire.org"
        ),
        Service(
            id = "cloth_sharing_life_closet",
            name = "Sharing Life Clothing Closet",
            category = Category.CLOTHING,
            address = "3544 E Emporium Cir, Mesquite, TX 75150",
            phone = "(972) 285-5262",
            hours = "MON 09:00-16:00; TUE 09:00-16:00; WED 09:00-16:00; THU 09:00-16:00",
            latitude = 32.8126,
            longitude = -96.6155,
            faithBased = false,
            blurb = "Free clothing distribution alongside the food pantry.",
            website = "https://sharinglife.org"
        ),
        Service(
            id = "cloth_mission_arlington",
            name = "Mission Arlington Clothing Bank",
            category = Category.CLOTHING,
            address = "210 W South St, Arlington, TX 76010",
            phone = "(817) 277-6620",
            hours = "MON 09:00-16:00; TUE 09:00-16:00; WED 09:00-16:00; THU 09:00-16:00; FRI 09:00-16:00",
            latitude = 32.7270,
            longitude = -97.1102,
            faithBased = true,
            blurb = "Free clothes, beds, and household basics to families in need. Christian ministry, open to all.",
            website = "https://missionarlington.org"
        ),
        Service(
            id = "cloth_dress_for_success",
            name = "Dress for Success Dallas",
            category = Category.CLOTHING,
            address = "9351 Skillman St Suite C, Dallas, TX 75243",
            phone = "(214) 343-5300",
            hours = "TUE 10:00-15:00; WED 10:00-15:00; THU 10:00-15:00",
            latitude = 32.8989,
            longitude = -96.7290,
            faithBased = false,
            blurb = "Free professional attire and career coaching for women. Referral encouraged.",
            website = "https://dallas.dressforsuccess.org"
        ),
        Service(
            id = "cloth_tarrant_churches_together",
            name = "Tarrant Churches Together Clothing Pantry",
            category = Category.CLOTHING,
            address = "501 W Northside Dr, Fort Worth, TX 76164",
            phone = "(817) 877-1664",
            hours = "MON 09:00-12:00; WED 09:00-12:00; FRI 09:00-12:00",
            latitude = 32.7787,
            longitude = -97.3414,
            faithBased = true,
            blurb = "Coalition of Tarrant churches; provides clothing vouchers and emergency aid.",
            website = "https://tarrantchurchestogether.org"
        ),

        // --------------- SHELTER ---------------
        Service(
            id = "shelter_bridge_homeless",
            name = "The Bridge Homeless Recovery Center",
            category = Category.SHELTER,
            address = "1818 Corsicana St, Dallas, TX 75201",
            phone = "(214) 670-1100",
            hours = "ALL 00:00-23:59",
            latitude = 32.7780,
            longitude = -96.7867,
            faithBased = false,
            blurb = "24/7 homeless recovery center providing shelter, meals, medical care, and case management.",
            website = "https://bridgehrc.org"
        ),
        Service(
            id = "shelter_austin_street",
            name = "Austin Street Center",
            category = Category.SHELTER,
            address = "1717 Jeffries St, Dallas, TX 75215",
            phone = "(214) 428-4242",
            hours = "ALL 17:00-23:59,00:00-08:00",
            latitude = 32.7741,
            longitude = -96.7773,
            faithBased = false,
            blurb = "Emergency overnight shelter with case management and housing support.",
            website = "https://austinstreet.org"
        ),
        Service(
            id = "shelter_union_gospel_dallas",
            name = "Union Gospel Mission of Dallas",
            category = Category.SHELTER,
            address = "3211 Irving Blvd, Dallas, TX 75247",
            phone = "(214) 638-2988",
            hours = "ALL 00:00-23:59",
            latitude = 32.7937,
            longitude = -96.8483,
            faithBased = true,
            blurb = "Christian shelter providing emergency beds, meals, and recovery programs for men, women, and families.",
            website = "https://ugmdallas.org"
        ),
        Service(
            id = "shelter_union_gospel_tarrant",
            name = "Union Gospel Mission of Tarrant County",
            category = Category.SHELTER,
            address = "1331 E Lancaster Ave, Fort Worth, TX 76102",
            phone = "(817) 338-9886",
            hours = "ALL 00:00-23:59",
            latitude = 32.7449,
            longitude = -97.3133,
            faithBased = true,
            blurb = "Emergency shelter, addiction recovery, and life-skills programs in central Fort Worth.",
            website = "https://ugm-tc.org"
        ),
        Service(
            id = "shelter_presbyterian_night",
            name = "Presbyterian Night Shelter",
            category = Category.SHELTER,
            address = "2400 Cypress St, Fort Worth, TX 76102",
            phone = "(817) 632-7400",
            hours = "ALL 00:00-23:59",
            latitude = 32.7637,
            longitude = -97.3041,
            faithBased = true,
            blurb = "Largest homeless shelter in Tarrant County. Beds, meals, healthcare, and housing programs.",
            website = "https://journeyhome.org"
        ),
        Service(
            id = "shelter_genesis_womens",
            name = "Genesis Women's Shelter",
            category = Category.SHELTER,
            address = "Confidential location — call first",
            phone = "(214) 946-4357",
            hours = "ALL 00:00-23:59",
            latitude = 32.8139,
            longitude = -96.8716,
            faithBased = false,
            blurb = "Emergency shelter for women and children fleeing domestic violence. 24/7 hotline; address withheld for safety.",
            website = "https://genesisshelter.org"
        ),
        Service(
            id = "shelter_safehaven_tarrant",
            name = "SafeHaven of Tarrant County",
            category = Category.SHELTER,
            address = "Confidential — call first",
            phone = "(877) 701-7233",
            hours = "ALL 00:00-23:59",
            latitude = 32.7555,
            longitude = -97.3308,
            faithBased = false,
            blurb = "Domestic violence shelter and 24/7 hotline serving Tarrant County. Address kept confidential.",
            website = "https://safehaventc.org"
        ),
        Service(
            id = "shelter_family_gateway",
            name = "Family Gateway",
            category = Category.SHELTER,
            address = "711 S St Paul St, Dallas, TX 75201",
            phone = "(214) 823-4500",
            hours = "ALL 00:00-23:59",
            latitude = 32.7785,
            longitude = -96.7918,
            faithBased = false,
            blurb = "Emergency shelter and supportive housing for families with children experiencing homelessness.",
            website = "https://familygateway.org"
        ),
        Service(
            id = "shelter_samaritan_inn",
            name = "The Samaritan Inn",
            category = Category.SHELTER,
            address = "1710 N McDonald St, McKinney, TX 75071",
            phone = "(972) 542-5302",
            hours = "ALL 00:00-23:59",
            latitude = 33.2202,
            longitude = -96.6201,
            faithBased = false,
            blurb = "Collin County's largest homeless program. Provides temporary housing, meals, case management, and life-skills training to help residents transition to permanent housing.",
            website = "https://thesamaritaninn.org"
        ),
        Service(
            id = "shelter_hope_center_irving",
            name = "Hope's Door New Beginning Center",
            category = Category.SHELTER,
            address = "Confidential — call first",
            phone = "(972) 422-7233",
            hours = "ALL 00:00-23:59",
            latitude = 32.9756,
            longitude = -96.6989,
            faithBased = false,
            blurb = "Emergency shelter and 24/7 hotline for survivors of domestic violence in Collin and northern Dallas County.",
            website = "https://hdnbc.org"
        ),
        Service(
            id = "shelter_salvation_army_carr",
            name = "Salvation Army Carr P. Collins Center",
            category = Category.SHELTER,
            address = "5302 Harry Hines Blvd, Dallas, TX 75235",
            phone = "(214) 424-7000",
            hours = "ALL 00:00-23:59",
            latitude = 32.8278,
            longitude = -96.8396,
            faithBased = true,
            blurb = "Emergency and transitional shelter for men, women, and families. Meals, case management, and substance-recovery programs on site.",
            website = "https://salvationarmydfw.org"
        ),
        Service(
            id = "food_st_vincent_de_paul_dallas",
            name = "St. Vincent de Paul of North Texas",
            category = Category.FOOD,
            address = "3052 W Northwest Hwy, Dallas, TX 75220",
            phone = "(972) 743-0696",
            hours = "TUE 09:00-12:00; THU 09:00-12:00; SAT 09:00-12:00",
            latitude = 32.8716,
            longitude = -96.8709,
            faithBased = true,
            blurb = "Catholic-affiliated network of food pantries serving low-income households across North Texas. Open to all.",
            website = "https://svdpdallas.org"
        )
    )
}
