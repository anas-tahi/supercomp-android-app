import mongoose from "mongoose";

const shoppingListSchema = new mongoose.Schema({
  user:  { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
  name:  { type: String, required: true, trim: true },
  items: { type: [String], required: true, default: [] }
}, { timestamps: true });

export default mongoose.model("ShoppingList", shoppingListSchema);
