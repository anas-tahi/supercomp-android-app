import express from "express";
import ShoppingList from "../models/ShoppingList.js";
import auth from "../middleware/auth.js";

const router = express.Router();

// GET all lists for a user 
router.get("/user/:userId", auth, async (req, res) => {
  try {
    if (req.params.userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only access your own shopping lists." });
    }
    const lists = await ShoppingList.find({ user: req.params.userId }).populate("products");
    res.json(lists);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST-create a new list
router.post("/", auth, async (req, res) => {
  try {
    const { user, name, products } = req.body;
        if (user !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only create shopping lists for yourself." });
    }
    
    if (!user || !name || !products?.length) 
      return res.status(400).json({ error: "user, name and products required." });
    
    let list = await ShoppingList.create({ user, name, products });
    list = await list.populate("products");
    res.status(201).json(list);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE a list 
router.delete("/:id", auth, async (req, res) => {
  try {
    const list = await ShoppingList.findById(req.params.id);
    if (!list) {
      return res.status(404).json({ error: "Shopping list not found." });
    }
        if (list.user.toString() !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only delete your own shopping lists." });
    }
    
    await ShoppingList.findByIdAndDelete(req.params.id);
    res.json({ message: "List deleted." });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
