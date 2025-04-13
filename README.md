# CareCycle


---

# CareCycle

**CareCycle** is a community-driven Android application that helps reduce food and product waste while supporting people in need. Individuals and restaurants can donate leftover food within a 1km radius, while expired or soon-to-expire packaged and personal care items are collected weekly. These items are recycled into animal feed, fertilizers, biogas, cleaning, or beauty products. Donors earn coins that can be converted into vouchers or coupons through partnered brands, creating a sustainable and rewarding ecosystem.

---

## 🌟 Key Features

- 🍛 **Nearby Food Donations**: Share leftover food with people nearby (1km radius).
- 🧼 **Expired Product Collection**: Weekly pickup of expired/near-expiry items.
- ♻️ **Recycling & Upcycling**: Use collected items to create makeup, cleaning products, and bio-based solutions.
- 🎁 **Coin & Voucher System**: Earn coins for donating, convert them into shopping vouchers through partners.
- 🗺️ **Location-Based Matching**: Google Maps API integration for proximity-based connections.
- 📸 **Image Uploading**: Use imgbb API for photo-based listings.

---

## 🔐 Tech Stack

- **Frontend**: Kotlin + XML (Android Studio)
- **Backend**: Firebase Realtime Database
- **Authentication**: Firebase Authentication
- **Image Storage**: imgbb API
- **Location Services**: Google Maps API

---

## 🔄 How It Works

1. Users register and authenticate using Firebase Authentication.
2. Donors upload items with descriptions, expiry, and photos using imgbb.
3. Nearby users can view and communicate to receive the food.
4. Expired or near-expiry items are scheduled for weekly pickups.
5. Donors earn coins (15 for food donations + 5 per plate delivered).
6. Coins can be exchanged for brand coupons or vouchers.
Got it! Here's how you can update your `README.md` to:



---

### 📽️ Demo Video

Watch the demo:  
👉 [**Click here to watch the video**](https://drive.google.com/drive/folders/1Vo29O6sdEPWe3i_FPA5NgNQCcPrWmcVF)

---

### 📊 Project Presentation

View the project slides:  
👉 [**Click here to view the PPT**](https://drive.google.com/drive/folders/1Vo29O6sdEPWe3i_FPA5NgNQCcPrWmcVF)

---

### 📥 Clone the Project from GitHub

```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

---

### 🔑 Update the `imgBB` API Key

1. Open the file:  
   `app/src/main/java/com/example/carecycle/Utils.kt`

2. Find the line with the placeholder API key and replace it like this:

```kotlin
const val IMGBB_API_KEY = "your_actual_imgbb_api_key_here"
```

---

## 🎯 Use Cases

- Restaurants donating leftover food to nearby people in real time.
- Households disposing expired personal care items responsibly.
- NGOs and volunteers sourcing donations for social impact.
- Donors getting rewarded with usable vouchers from partner brands.

---

## 🚧 Dependencies / Show Stoppers

- Active participation from local communities.
- Internet access for real-time features.
- Reliable pickup and recycling logistics.
- Consistent brand partnership for vouchers and rewards.

---

## 📦 Future Scope

- Build a delivery volunteer network.
- Expand donation radius and scheduling.
- In-app coin wallet and redemption history.
- Push notifications for nearby donations and pickups.

---
