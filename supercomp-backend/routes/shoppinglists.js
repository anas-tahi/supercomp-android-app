import express from "express";
import ShoppingList from "../models/ShoppingList.js";

const router = express.Router();

// READ with FILTER – get all lists for a user
router.get("/user/:userId", async (req, res) => {
  try {
    const lists = await ShoppingList.find({ user: req.params.userId });
    res.json(lists);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// WRITE – create a new shopping list
router.post("/", async (req, res) => {
  try {
    const { user, name, items } = req.body;
    await ShoppingList.create({ user, name, items });
    res.json({ message: "Shopping list created" });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE – remove a shopping list
router.delete("/:id", async (req, res) => {
  try {
    await ShoppingList.findByIdAndDelete(req.params.id);
    res.json({ message: "Shopping list deleted" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
