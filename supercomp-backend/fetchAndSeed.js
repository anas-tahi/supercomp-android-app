// fetchAndSeed.js
// 1. Clears ALL existing products from MongoDB
// 2. Tries to fetch real products from Mercadona's public API
// 3. If API fails → uses built-in Spanish supermarket dataset
// 4. Creates entries for: Mercadona, Lidl, Carrefour, Alcampo
//
// Run: node fetchAndSeed.js

import mongoose from "mongoose";
import dotenv from "dotenv";
import https from "https";

dotenv.config();

// ── Product Model ─────────────────────────────────────────────────────────────
const productSchema = new mongoose.Schema({
  name:        { type: String, required: true, trim: true },
  supermarket: { type: String, required: true, enum: ["Mercadona", "Lidl", "Carrefour", "Alcampo"] },
  price:       { type: Number, required: true },
  category:    { type: String, default: "General" },
  imageUrl:    { type: String, default: "" }
}, { timestamps: true });

const Product = mongoose.model("Product", productSchema);

// ── 4 supermarkets with price factors vs Mercadona ───────────────────────────
// Based on real OCU and FACUA average comparison data (Spain 2025)
const SUPERMARKETS = {
  Mercadona: 1.00,
  Lidl:      0.94,   // ~6% cheaper
  Carrefour: 1.06,   // ~6% more expensive
  Alcampo:   1.02,   // ~2% more expensive
};

// ── Mercadona category IDs ────────────────────────────────────────────────────
const CATEGORIES = [
  { id: "112", name: "Leche" },
  { id: "113", name: "Yogures" },
  { id: "114", name: "Quesos" },
  { id: "115", name: "Mantequilla" },
  { id: "118", name: "Huevos" },
  { id: "151", name: "Aceite de oliva" },
  { id: "153", name: "Aceite de girasol" },
  { id: "121", name: "Pan" },
  { id: "133", name: "Arroz" },
  { id: "134", name: "Pasta" },
  { id: "135", name: "Legumbres" },
  { id: "141", name: "Fruta" },
  { id: "142", name: "Verduras" },
  { id: "161", name: "Pollo y pavo" },
  { id: "171", name: "Pescado" },
  { id: "181", name: "Agua" },
  { id: "182", name: "Refrescos" },
  { id: "183", name: "Zumos" },
  { id: "191", name: "Galletas" },
  { id: "193", name: "Chocolates" },
];

// ── Realistic price ending ────────────────────────────────────────────────────
function realisticPrice(base, factor) {
  const raw = base * factor;
  const floor = Math.floor(raw);
  const endings = [0.99, 0.89, 0.79, 0.69, 0.59, 0.49, 0.29, 0.19];
  const ending = endings.find(e => floor + e <= raw + 0.25) ?? 0.99;
  return parseFloat((floor + ending).toFixed(2));
}

// ── HTTP helper ───────────────────────────────────────────────────────────────
function fetchJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, {
      headers: {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        "Accept": "application/json",
        "Accept-Language": "es-ES,es;q=0.9",
        "Referer": "https://tienda.mercadona.es/",
      }
    }, (res) => {
      let data = "";
      res.on("data", c => data += c);
      res.on("end", () => {
        try { resolve(JSON.parse(data)); }
        catch (e) { reject(new Error("Parse error")); }
      });
    }).on("error", reject);
  });
}

// ── Build docs for all 4 supermarkets ────────────────────────────────────────
function buildDocs(name, mercPrice, category, imageUrl) {
  return Object.entries(SUPERMARKETS).map(([supermarket, factor]) => ({
    name,
    supermarket,
    price: supermarket === "Mercadona"
      ? parseFloat(mercPrice.toFixed(2))
      : realisticPrice(mercPrice, factor),
    category,
    imageUrl,
  }));
}

// ── Try Mercadona API ─────────────────────────────────────────────────────────
async function fetchFromMercadona() {
  const docs = [];
  let fetched = 0;
  let failed = 0;

  for (const cat of CATEGORIES) {
    try {
      process.stdout.write(`  Fetching ${cat.name}... `);
      const url = `https://tienda.mercadona.es/api/categories/${cat.id}/?lang=es&wh=mad1`;
      const data = await fetchJson(url);
      const subcategories = data.categories || [];

      for (const subcat of subcategories) {
        for (const p of (subcat.products || [])) {
          const name = p.display_name?.trim();
          const price = parseFloat(p.price_instructions?.unit_price || 0);
          const imageUrl = p.photos?.[0]?.zoom || p.photos?.[0]?.regular || "";
          if (!name || price <= 0) continue;
          docs.push(...buildDocs(name, price, cat.name, imageUrl));
          fetched++;
        }
      }

      console.log(`✅ ${fetched} products so far`);
      await new Promise(r => setTimeout(r, 400));
    } catch (err) {
      console.log(`⚠️  skipped`);
      failed++;
    }
  }

  return { docs, fetched, failed };
}

// ── Built-in fallback dataset ─────────────────────────────────────────────────
function getFallbackDocs() {
  const items = [
    // Lácteos
    { name: "Leche Entera 1L",               cat: "Leche",        img: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400", price: 0.96 },
    { name: "Leche Semidesnatada 1L",         cat: "Leche",        img: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", price: 0.94 },
    { name: "Leche Desnatada 1L",             cat: "Leche",        img: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400", price: 0.90 },
    { name: "Yogur Natural Pack x4",          cat: "Yogures",      img: "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", price: 0.72 },
    { name: "Yogur Griego Natural 500g",      cat: "Yogures",      img: "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", price: 1.45 },
    { name: "Queso Semicurado 250g",          cat: "Quesos",       img: "https://images.unsplash.com/photo-1618164435735-413d3b066c9a?w=400", price: 2.45 },
    { name: "Queso Fresco 250g",              cat: "Quesos",       img: "https://images.unsplash.com/photo-1618164435735-413d3b066c9a?w=400", price: 1.35 },
    { name: "Mantequilla 250g",               cat: "Mantequilla",  img: "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=400", price: 1.85 },
    // Huevos
    { name: "Huevos Camperos L (12 u)",       cat: "Huevos",       img: "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400", price: 2.65 },
    { name: "Huevos M (6 u)",                 cat: "Huevos",       img: "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400", price: 1.15 },
    // Aceites
    { name: "Aceite de Oliva Virgen Extra 1L",cat: "Aceites",      img: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400", price: 7.25 },
    { name: "Aceite de Girasol 1L",           cat: "Aceites",      img: "https://images.unsplash.com/photo-1519915028121-7d3463d20b13?w=400", price: 1.89 },
    // Pan & Cereales
    { name: "Pan de Molde Blanco 500g",       cat: "Pan",          img: "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400", price: 1.15 },
    { name: "Pan de Molde Integral 500g",     cat: "Pan",          img: "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400", price: 1.35 },
    { name: "Arroz Redondo 1kg",              cat: "Arroz",        img: "https://images.unsplash.com/photo-1536304993881-ff86e5278462?w=400", price: 0.99 },
    { name: "Arroz Largo 1kg",                cat: "Arroz",        img: "https://images.unsplash.com/photo-1536304993881-ff86e5278462?w=400", price: 1.15 },
    { name: "Pasta Espagueti 500g",           cat: "Pasta",        img: "https://images.unsplash.com/photo-1551462147-ff29053bfc14?w=400", price: 0.65 },
    { name: "Macarrones 500g",                cat: "Pasta",        img: "https://images.unsplash.com/photo-1551462147-ff29053bfc14?w=400", price: 0.65 },
    { name: "Galletas María 400g",            cat: "Galletas",     img: "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400", price: 0.85 },
    { name: "Cereales de Desayuno 500g",      cat: "Cereales",     img: "https://images.unsplash.com/photo-1517093728432-a0440f8d45af?w=400", price: 1.95 },
    { name: "Azúcar Blanco 1kg",              cat: "Cereales",     img: "https://images.unsplash.com/photo-1536304993881-ff86e5278462?w=400", price: 0.89 },
    // Legumbres
    { name: "Lentejas 500g",                  cat: "Legumbres",    img: "https://images.unsplash.com/photo-1611171711791-b34e61d34bf4?w=400", price: 0.79 },
    { name: "Garbanzos Cocidos 400g",         cat: "Legumbres",    img: "https://images.unsplash.com/photo-1611171711791-b34e61d34bf4?w=400", price: 0.69 },
    // Carne
    { name: "Pechuga de Pollo 1kg",           cat: "Pollo y pavo", img: "https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=400", price: 5.95 },
    { name: "Contramuslos de Pollo 1kg",      cat: "Pollo y pavo", img: "https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=400", price: 3.95 },
    { name: "Carne Picada Mixta 400g",        cat: "Carne",        img: "https://images.unsplash.com/photo-1558030006-450675393462?w=400", price: 2.99 },
    { name: "Jamón Serrano 100g",             cat: "Charcutería",  img: "https://images.unsplash.com/photo-1608198093002-ad4e005484ec?w=400", price: 1.95 },
    { name: "Jamón Cocido 150g",              cat: "Charcutería",  img: "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400", price: 1.65 },
    { name: "Chorizo Extra 200g",             cat: "Charcutería",  img: "https://images.unsplash.com/photo-1624811533744-f85d5325d49c?w=400", price: 1.75 },
    // Pescado
    { name: "Atún en Aceite (pack 3)",        cat: "Pescado",      img: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400", price: 1.85 },
    { name: "Salmón Ahumado 100g",            cat: "Pescado",      img: "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400", price: 2.55 },
    { name: "Sardinas en Tomate (pack 3)",    cat: "Pescado",      img: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400", price: 1.25 },
    // Frutas y Verduras
    { name: "Tomates Rama 1kg",               cat: "Verduras",     img: "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400", price: 1.79 },
    { name: "Lechuga Iceberg",                cat: "Verduras",     img: "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400", price: 0.89 },
    { name: "Patatas 2kg",                    cat: "Verduras",     img: "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400", price: 1.85 },
    { name: "Cebolla 1kg",                    cat: "Verduras",     img: "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400", price: 0.89 },
    { name: "Zanahoria 1kg",                  cat: "Verduras",     img: "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400", price: 0.79 },
    { name: "Plátanos de Canarias 1kg",       cat: "Fruta",        img: "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400", price: 1.85 },
    { name: "Manzanas Golden 1kg",            cat: "Fruta",        img: "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", price: 1.95 },
    { name: "Naranjas de Mesa 1kg",           cat: "Fruta",        img: "https://images.unsplash.com/photo-1547514701-42782101795e?w=400", price: 1.49 },
    { name: "Peras Conferencia 1kg",          cat: "Fruta",        img: "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", price: 1.69 },
    // Bebidas
    { name: "Agua Mineral 6x1.5L",            cat: "Agua",         img: "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400", price: 1.85 },
    { name: "Refresco Cola 2L",               cat: "Refrescos",    img: "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400", price: 1.35 },
    { name: "Zumo de Naranja 1L",             cat: "Zumos",        img: "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400", price: 1.45 },
    { name: "Café Molido Natural 250g",       cat: "Café",         img: "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400", price: 2.15 },
    { name: "Infusión Manzanilla (25 u)",     cat: "Café",         img: "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400", price: 0.79 },
    // Chocolates
    { name: "Chocolate Negro 70% 100g",       cat: "Chocolates",   img: "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400", price: 0.89 },
    { name: "Chocolate con Leche 100g",       cat: "Chocolates",   img: "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400", price: 0.79 },
    // Higiene & Limpieza
    { name: "Papel Higiénico 12 rollos",      cat: "Higiene",      img: "https://images.unsplash.com/photo-1584556812952-905ffd0c611a?w=400", price: 3.45 },
    { name: "Gel de Ducha 750ml",             cat: "Higiene",      img: "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400", price: 1.25 },
    { name: "Champú Normal 400ml",            cat: "Higiene",      img: "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400", price: 1.45 },
    { name: "Detergente Líquido 30 lav",      cat: "Limpieza",     img: "https://images.unsplash.com/photo-1585843149061-71d07a1db8f3?w=400", price: 4.95 },
  ];

  const docs = [];
  for (const item of items) {
    docs.push(...buildDocs(item.name, item.price, item.cat, item.img));
  }
  return docs;
}

// ── Main ──────────────────────────────────────────────────────────────────────
async function main() {
  try {
    console.log("🔌 Connecting to MongoDB...");
    await mongoose.connect(process.env.MONGO_URI);
    console.log("✅ Connected\n");

    // Step 1: Delete ALL existing products
    const deleted = await Product.deleteMany({});
    console.log(`🗑️  Deleted ${deleted.deletedCount} existing products\n`);

    // Step 2: Try Mercadona API
    console.log("📡 Trying Mercadona API...\n");
    let docs = [];

    try {
      const result = await fetchFromMercadona();
      if (result.fetched > 0) {
        docs = result.docs;
        console.log(`\n✅ Fetched ${result.fetched} real products from Mercadona`);
        console.log(`⚠️  ${result.failed} categories skipped\n`);
      } else {
        throw new Error("No products returned");
      }
    } catch (err) {
      console.log(`\n⚠️  Mercadona API unavailable: ${err.message}`);
      console.log("📦 Using built-in dataset instead...\n");
      docs = getFallbackDocs();
    }

    // Step 3: Insert everything
    await Product.insertMany(docs);
    const uniqueProducts = docs.length / 4;
    console.log(`✅ Inserted ${docs.length} entries`);
    console.log(`   ${uniqueProducts} unique products × 4 supermarkets`);
    console.log(`   Supermarkets: Mercadona · Lidl · Carrefour · Alcampo`);
    console.log("\n🎉 Database seeded successfully!");

  } catch (err) {
    console.error("❌ Fatal error:", err.message);
  } finally {
    await mongoose.disconnect();
    console.log("🔌 Disconnected from MongoDB");
  }
}

main();
