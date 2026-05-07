import express from "express";
import Product from "../models/Product.js";

const router = express.Router();

// GET all products
router.get("/", async (req, res) => {
  try {
    const products = await Product.find();
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET products by supermarket
router.get("/supermarket/:name", async (req, res) => {
  try {
    const products = await Product.find({ supermarket: req.params.name });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET products by search name (for compare)
router.get("/search", async (req, res) => {
  try {
    const { name } = req.query;
    if (!name) return res.status(400).json({ error: "Query param 'name' required." });
    const products = await Product.find({ name: { $regex: name, $options: "i" } });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST create product (admin use / seed)
router.post("/", async (req, res) => {
  try {
    const { name, supermarket, price, category, imageUrl } = req.body;
    const product = await Product.create({ name, supermarket, price, category, imageUrl });
    res.status(201).json(product);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

export default router;
