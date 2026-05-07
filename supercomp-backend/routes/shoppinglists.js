import express from "express";
import ShoppingList from "../models/ShoppingList.js";
import auth from "../middleware/auth.js";

const router = express.Router();

// GET all lists for a user (protected)
router.get("/user/:userId", auth, async (req, res) => {
  try {
    const lists = await ShoppingList.find({ user: req.params.userId }).populate("products");
    res.json(lists);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST create a new list (protected)
router.post("/", auth, async (req, res) => {
  try {
    const { user, name, products } = req.body;
    if (!user || !products?.length) return res.status(400).json({ error: "user and products required." });
    const list = await ShoppingList.create({ user, name, products });
    res.status(201).json(list);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE a list (protected)
router.delete("/:id", auth, async (req, res) => {
  try {
    await ShoppingList.findByIdAndDelete(req.params.id);
    res.json({ message: "List deleted." });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
