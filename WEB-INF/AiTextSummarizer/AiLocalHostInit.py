from flask import Flask, request, jsonify
from transformers import BartTokenizer, BartForConditionalGeneration
import torch
from waitress import serve
from torch.amp import autocast
import traceback
import warnings
# Suppress specific warnings
warnings.filterwarnings("ignore")
import transformers
transformers.logging.set_verbosity_error()

# Initialize Flask app
app = Flask(__name__)

# Load the tokenizer and model from the local path
local_model_path = "./bart-large-cnn"
tokenizer = BartTokenizer.from_pretrained(local_model_path)
model = BartForConditionalGeneration.from_pretrained(local_model_path)

# Move the model to GPU if available
device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)

@app.route('/summarize', methods=['POST'])
def summarize():
    try:
        data = request.json
        text = data.get("inputs", "")
        max_length = data.get("max_length", 180)
        min_length = data.get("min_length", 100)
        do_sample = data.get("do_sample", True)
        num_beams = data.get("num_beams", 3)

        # Input validation
        if not isinstance(text, str):
            return jsonify({"error": "'text' must be a string"}), 400
        if not text:
            return jsonify({"error": "Missing 'text' field in request"}), 400
        if not isinstance(max_length, int) or not isinstance(min_length, int):
            return jsonify({"error": "'max_length' and 'min_length' must be integers"}), 400
        if max_length < min_length:
            return jsonify({"error": "max_length must be greater than or equal to min_length"}), 400
        if num_beams <= 0:
            return jsonify({"error": "num_beams must be greater than 0"}), 400

        # Tokenize and generate summary
        inputs = tokenizer.encode(text, return_tensors="pt", max_length=1024, truncation=True).to(device)
        if device == "cuda":
            with autocast("cuda"):
                summary_ids = model.generate(
                    inputs,
                    max_length=max_length,
                    min_length=min_length,
                    do_sample=do_sample,
                    num_beams=num_beams
                )
        else:
            summary_ids = model.generate(
                inputs,
                max_length=max_length,
                min_length=min_length,
                do_sample=do_sample,
                num_beams=num_beams
            )
        summary = tokenizer.decode(summary_ids[0], skip_special_tokens=True)

        return jsonify([{"summary_text": summary}])

    except Exception as e:
        traceback.print_exc() 
        return jsonify({"error": "An internal server error occurred"}), 500

if __name__ == '__main__':
    port = 5000
    threads = 2
    print(f"Starting summarization API with Waitress...")
    print(f"Server is running on port {port} with {threads} worker threads.")
    print(f"Visit http://localhost:{port}/summarize for the AI!")
    serve(app, host='0.0.0.0', port=port, threads=threads)