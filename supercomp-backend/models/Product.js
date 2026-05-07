import mongoose from "mongoose";

const productSchema = new mongoose.Schema({
  name:        { type: String, required: true, trim: true },
  supermarket: {
    type: String,
    required: true,
    enum: ["Mercadona", "Lidl", "Carrefour", "Alcampo"]
  },
  price:       { type: Number, required: true },
  category:    { type: String, default: "General" },
  imageUrl:    { type: String, default: "" }
}, { timestamps: true });

export default mongoose.model("Product", productSchema);
