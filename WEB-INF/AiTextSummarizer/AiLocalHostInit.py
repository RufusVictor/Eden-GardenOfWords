from flask import Flask, request, jsonify
from transformers import BartTokenizer, BartForConditionalGeneration
import torch
from waitress import serve
import traceback
import warnings

# Suppress warnings for cleaner logs
warnings.filterwarnings("ignore")
import transformers
transformers.logging.set_verbosity_error()

# Initialize Flask app
app = Flask(__name__)

# Load model and tokenizer
local_model_path = "./bart-large-cnn"
print("Loading BART tokenizer and model...")
tokenizer = BartTokenizer.from_pretrained(local_model_path)
model = BartForConditionalGeneration.from_pretrained(local_model_path)

# Set device: CUDA (NVIDIA GPU), MPS (Apple Silicon), or CPU
if torch.cuda.is_available():
    device = "cuda"
    print("✅ Using CUDA GPU")
elif hasattr(torch.backends, 'mps') and torch.backends.mps.is_available():
    device = "mps"
    print("✅ Using Apple Silicon MPS")
else:
    device = "cpu"
    print("✅ Using CPU")

model.to(device)
model.eval()  # Set to inference mode for safety/performance

# 🛡️ CRITICAL FIX: Enforce generation config to avoid NoneType crash
model.generation_config.length_penalty = 1.0
model.generation_config.early_stopping = True
model.generation_config.no_repeat_ngram_size = 3
model.generation_config.pad_token_id = tokenizer.pad_token_id
model.generation_config.eos_token_id = tokenizer.eos_token_id

@app.route('/summarize', methods=['POST'])
def summarize():
    try:
        data = request.json
        if not data:  # ← ✅ FIXED: was "if not " → SyntaxError
            return jsonify({"error": "Request body must be valid JSON"}), 400

        text = data.get("inputs", "").strip()
        max_length = data.get("max_length", 180)
        min_length = data.get("min_length", 100)
        do_sample = data.get("do_sample", False)  # Better default for summarization
        num_beams = data.get("num_beams", 4)

        # Input validation
        if not isinstance(text, str):
            return jsonify({"error": "'inputs' must be a string"}), 400
        if len(text) == 0:
            return jsonify({"error": "'inputs' text cannot be empty"}), 400
        if not isinstance(max_length, int) or not isinstance(min_length, int):
            return jsonify({"error": "'max_length' and 'min_length' must be integers"}), 400
        if max_length < min_length:
            return jsonify({"error": "'max_length' must be >= 'min_length'"}), 400
        if max_length > 1024:
            return jsonify({"error": "'max_length' must be <= 1024"}), 400
        if min_length < 10:
            return jsonify({"error": "'min_length' must be >= 10"}), 400
        if not isinstance(num_beams, int) or num_beams <= 0:
            return jsonify({"error": "'num_beams' must be a positive integer"}), 400

        # Tokenize input
        inputs = tokenizer(
            text,
            return_tensors="pt",
            max_length=1024,
            truncation=True,
            padding=True
        ).to(device)

        # Generate summary — safe and clean
        with torch.no_grad():
            summary_ids = model.generate(
                inputs["input_ids"],
                attention_mask=inputs.get("attention_mask"),
                max_length=max_length,
                min_length=min_length,
                do_sample=do_sample,
                num_beams=num_beams,
            )

        summary = tokenizer.decode(summary_ids[0], skip_special_tokens=True)

        return jsonify([{"summary_text": summary}])

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": "Internal server error", "details": str(e)}), 500


if __name__ == '__main__':
    port = 5000
    threads = 4

    print(f"\n🚀 Starting BART Summarization API with Waitress...")
    print(f"   → Device: {device.upper()}")
    print(f"   → Model: {local_model_path}")
    print(f"   → Server: http://localhost:{port}/summarize")
    print(f"   → Worker threads: {threads}")
    print(f"   → Send POST with {{\"inputs\": \"your text...\", \"max_length\": 150, \"min_length\": 50}}\n")

    serve(app, host='0.0.0.0', port=port, threads=threads)
