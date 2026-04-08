import express from "express";
import Product from "../models/Product.js";

const router = express.Router();

// READ – get all products
router.get("/", async (req, res) => {
  try {
    const products = await Product.find();
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// READ with FILTER – get products by supermarket
router.get("/supermarket/:name", async (req, res) => {
  try {
    const products = await Product.find({ supermarket: req.params.name });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// READ with FILTER – search products by name (for comparison)
router.get("/search", async (req, res) => {
  try {
    const { name } = req.query;
    const products = await Product.find({
      name: { $regex: name, $options: "i" }
    });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
