# PROJECT OVERVIEW

![image](https://user-images.githubusercontent.com/68406409/222018620-ffca1680-8e28-400c-8b8a-c4e47d5db4be.png)


### Overview Folder

---

- Setiap service ada pada foldernya masing-masing (customer-service, order-service, payment-service, dan restaurant-service)
- Common folder
  - `/infrastructur/kafka`: Implementasi general Apache Kafka pada Java
  - `/common`: Reusable components yang dipakai oleh setiap services. Componentnya itu seperti `Entity` dan `ValueObject`. Ada juga `Exception`.
     
     ![image](https://user-images.githubusercontent.com/68406409/222018655-edf5aa0b-3bbb-42f5-b0cd-0dacf1f92889.png)


### Overview Service

---

Folder disusun berdasarkan Clean Architecture.

- `order-application`: (Layer 4) Framework, tempat routing dan controller
- `order-messaging`: (Layer 4) Implementasi Apache Kafka untuk publish dan listen message pada topic yang ditarget. Dalam kasus ini order-service, topic yang ditarget adalah
  - payment-request-topic
  - payment-response-topic
  - restaurant-approval-request-topic
  - restaurant-approval-response-topic.
- `order-dataaccess`: (Layer 4) Driver database,
- `order-domain/order-application-service`: (Layer 3) Tempat untuk:
  - Mengambil data dari database → meneruskan ke Domain Service (Layer 2) untuk diproses **business logic** → menyimpan perubahan ke database.
  - Mempublish event ke Kafka menggunakan abstraksi interface (alias tidak menggunakan secara langsung implementasi Apache Kafka yang ada pada Layer 4).
- `order-domain/order-domain-core`: (Layer 2) Berisikan busniess logic yang tidak cocok untuk ditempatkan pada Entity (Layer 1).
- `order-domain/order-domain-core/entity`: (Layer 1) Berisikan **business logic** suatu Entity.

> PS. **Business Logic** itu isinya murni logika-logika untuk mengubah state object saja


