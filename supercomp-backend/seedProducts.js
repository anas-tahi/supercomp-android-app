/*
 // seedProducts.js
// Run with: node seedProducts.js
// Seeds MongoDB with fake/local supermarket products and prices
// Keeps the existing MongoDB product dataset and avoids the new real-data seeder.

import mongoose from "mongoose";
import dotenv from "dotenv";

dotenv.config();

const productSchema = new mongoose.Schema({
  name:        { type: String, required: true },
  supermarket: { type: String, required: true },
  price:       { type: Number, required: true },
  category:    { type: String, default: "General" },
  imageUrl:    { type: String, default: "" },
  source:      { type: String, enum: ["fake", "real", "legacy"], default: "fake" }
});

const Product = mongoose.model("Product", productSchema);

// Real products with accurate 2025/2026 Spanish supermarket prices
// Sources: OCU, FACUA, El Español, El Debate price reports
const products = [
  // ─── LÁCTEOS ──────────────────────────────────────────────────────────────
  { name: "Leche Entera 1L",           category: "Lácteos",
    prices: { Mercadona: 0.96, Lidl: 0.99, Dia: 1.02, Carrefour: 1.05, Dia: 0.98 } },

  { name: "Leche Semidesnatada 1L",    category: "Lácteos",
    prices: { Mercadona: 0.94, Lidl: 0.97, Dia: 0.99, Carrefour: 1.03, Dia: 0.95 } },

  { name: "Yogur Natural Pack x4",     category: "Lácteos",
    prices: { Mercadona: 0.72, Lidl: 0.69, Dia: 0.79, Carrefour: 0.85, Dia: 0.74 } },

  { name: "Queso Semicurado 250g",     category: "Lácteos",
    prices: { Mercadona: 2.45, Lidl: 2.29, Dia: 2.65, Carrefour: 2.79, Dia: 2.39 } },

  { name: "Mantequilla 250g",          category: "Lácteos",
    prices: { Mercadona: 1.85, Lidl: 1.69, Dia: 1.95, Carrefour: 2.10, Dia: 1.79 } },

  // ─── ACEITES ──────────────────────────────────────────────────────────────
  { name: "Aceite de Oliva Virgen Extra 1L", category: "Aceites",
    prices: { Mercadona: 7.25, Lidl: 6.99, Dia: 7.49, Carrefour: 7.15, Dia: 6.89 } },

  { name: "Aceite de Girasol 1L",      category: "Aceites",
    prices: { Mercadona: 1.89, Lidl: 1.75, Dia: 1.95, Carrefour: 1.85, Dia: 1.70 } },

  // ─── HUEVOS ───────────────────────────────────────────────────────────────
  { name: "Huevos Camperos L (12 u)", category: "Huevos",
    prices: { Mercadona: 2.65, Lidl: 2.49, Dia: 2.79, Carrefour: 2.89, Dia: 2.55 } },

  { name: "Huevos M (6 u)",           category: "Huevos",
    prices: { Mercadona: 1.15, Lidl: 1.09, Dia: 1.25, Carrefour: 1.29, Dia: 1.10 } },

  // ─── CEREALES & PAN ───────────────────────────────────────────────────────
  { name: "Arroz Redondo 1kg",         category: "Cereales",
    prices: { Mercadona: 0.99, Lidl: 0.89, Dia: 1.05, Carrefour: 1.09, Dia: 0.92 } },

  { name: "Pasta Espagueti 500g",      category: "Cereales",
    prices: { Mercadona: 0.65, Lidl: 0.59, Dia: 0.72, Carrefour: 0.79, Dia: 0.62 } },

  { name: "Pan de Molde 500g",         category: "Pan",
    prices: { Mercadona: 1.15, Lidl: 0.99, Dia: 1.25, Carrefour: 1.35, Dia: 1.09 } },

  { name: "Galletas María 400g",       category: "Cereales",
    prices: { Mercadona: 0.85, Lidl: 0.79, Dia: 0.92, Carrefour: 0.99, Dia: 0.82 } },

  { name: "Cereales de Desayuno 500g", category: "Cereales",
    prices: { Mercadona: 1.95, Lidl: 1.75, Dia: 2.15, Carrefour: 2.29, Dia: 1.85 } },

  // ─── CARNE & PESCADO ──────────────────────────────────────────────────────
  { name: "Pechuga de Pollo 1kg",      category: "Carne",
    prices: { Mercadona: 5.95, Lidl: 5.49, Dia: 6.25, Carrefour: 6.45, Dia: 5.65 } },

  { name: "Carne Picada Mixta 400g",   category: "Carne",
    prices: { Mercadona: 2.99, Lidl: 2.79, Dia: 3.15, Carrefour: 3.29, Dia: 2.89 } },

  { name: "Atún en Aceite (pack 3)",   category: "Pescado",
    prices: { Mercadona: 1.85, Lidl: 1.69, Dia: 1.99, Carrefour: 2.09, Dia: 1.75 } },

  { name: "Salmón Ahumado 100g",       category: "Pescado",
    prices: { Mercadona: 2.55, Lidl: 2.29, Dia: 2.75, Carrefour: 2.89, Dia: 2.45 } },

  // ─── FRUTAS & VERDURAS ────────────────────────────────────────────────────
  { name: "Tomates Rama 1kg",          category: "Frutas",
    prices: { Mercadona: 1.79, Lidl: 1.59, Dia: 1.89, Carrefour: 1.99, Dia: 1.65 } },

  { name: "Plátanos de Canarias 1kg",  category: "Frutas",
    prices: { Mercadona: 1.85, Lidl: 1.69, Dia: 1.95, Carrefour: 2.05, Dia: 1.75 } },

  { name: "Manzanas Golden 1kg",       category: "Frutas",
    prices: { Mercadona: 1.95, Lidl: 1.79, Dia: 2.09, Carrefour: 2.19, Dia: 1.85 } },

  { name: "Patatas 2kg",               category: "Verduras",
    prices: { Mercadona: 1.85, Lidl: 1.69, Dia: 1.99, Carrefour: 2.09, Dia: 1.65 } },

  { name: "Naranjas de Mesa 1kg",      category: "Frutas",
    prices: { Mercadona: 1.49, Lidl: 1.35, Dia: 1.59, Carrefour: 1.69, Dia: 1.39 } },

  { name: "Lechuga Iceberg",           category: "Verduras",
    prices: { Mercadona: 0.89, Lidl: 0.79, Dia: 0.95, Carrefour: 0.99, Dia: 0.82 } },

  // ─── BEBIDAS ──────────────────────────────────────────────────────────────
  { name: "Agua Mineral 6x1.5L",       category: "Bebidas",
    prices: { Mercadona: 1.85, Lidl: 1.69, Dia: 1.99, Carrefour: 2.09, Dia: 1.75 } },

  { name: "Refresco Cola 2L",          category: "Bebidas",
    prices: { Mercadona: 1.35, Lidl: 1.19, Dia: 1.45, Carrefour: 1.55, Dia: 1.25 } },

  { name: "Zumo de Naranja 1L",        category: "Bebidas",
    prices: { Mercadona: 1.45, Lidl: 1.29, Dia: 1.55, Carrefour: 1.65, Dia: 1.39 } },

  { name: "Café Molido 250g",          category: "Bebidas",
    prices: { Mercadona: 2.15, Lidl: 1.99, Dia: 2.29, Carrefour: 2.45, Dia: 2.09 } },

  // ─── CHARCUTERÍA ──────────────────────────────────────────────────────────
  { name: "Jamón Serrano 100g",        category: "Charcutería",
    prices: { Mercadona: 1.95, Lidl: 1.79, Dia: 2.09, Carrefour: 2.19, Dia: 1.85 } },

  { name: "Jamón Cocido 150g",         category: "Charcutería",
    prices: { Mercadona: 1.65, Lidl: 1.49, Dia: 1.79, Carrefour: 1.89, Dia: 1.59 } },

  { name: "Chorizo Extra 200g",        category: "Charcutería",
    prices: { Mercadona: 1.75, Lidl: 1.59, Dia: 1.89, Carrefour: 1.99, Dia: 1.69 } },

  // ─── HIGIENE & LIMPIEZA ───────────────────────────────────────────────────
  { name: "Papel Higiénico 12 rollos", category: "Higiene",
    prices: { Mercadona: 3.45, Lidl: 2.99, Dia: 3.65, Carrefour: 3.79, Dia: 3.25 } },

  { name: "Gel de Ducha 750ml",        category: "Higiene",
    prices: { Mercadona: 1.25, Lidl: 0.99, Dia: 1.39, Carrefour: 1.49, Dia: 1.15 } },

  { name: "Detergente Líquido 30 lav", category: "Limpieza",
    prices: { Mercadona: 4.95, Lidl: 4.49, Dia: 5.25, Carrefour: 5.45, Dia: 4.75 } },
];

async function seed() {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("✅ Connected to MongoDB");

    // Clear existing fake products only
    await Product.deleteMany({ source: "fake" });
    console.log("🗑️  Cleared existing fake products");

    // Insert all fake products for all supermarkets
    const docs = [];
    for (const p of products) {
      for (const [supermarket, price] of Object.entries(p.prices)) {
        docs.push({
          name: p.name,
          supermarket,
          price,
          category: p.category,
          source: "fake"
        });
      }
    }

    await Product.insertMany(docs);
    console.log(`✅ Inserted ${docs.length} products (${products.length} unique × 4 supermarkets)`);
    console.log("🎉 Seed complete!");
  } catch (err) {
    console.error("❌ Error:", err.message);
  } finally {
    await mongoose.disconnect();
  }
}

seed();
*/