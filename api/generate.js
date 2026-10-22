// Vercel Serverless Function: /api/generate
// Handles Gemini API calls securely using process.env.GEMINI_API_KEY with offline synthesis fallback

module.exports = async function handler(req, res) {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const { topic = "Algorithms & Data Structures", category = "CS_TECH", difficulty = 2, count = 5 } = 
    req.method === 'POST' ? req.body : req.query;

  const apiKey = process.env.GEMINI_API_KEY;

  // If no API key configured or placeholder key, return high-quality offline synthesized items
  if (!apiKey || apiKey === 'YOUR_GEMINI_API_KEY_HERE' || apiKey === 'MY_GEMINI_API_KEY') {
    const offlineItems = generateOfflineMcqs(topic, category, parseInt(difficulty, 10), parseInt(count, 10));
    return res.status(200).json({
      source: 'OFFLINE_SYNTHESIS_ENGINE',
      warning: 'GEMINI_API_KEY not configured in Vercel environment variables. Using offline concept engine.',
      items: offlineItems
    });
  }

  // Schema for strict JSON output from Gemini
  const responseSchema = {
    type: "ARRAY",
    items: {
      type: "OBJECT",
      properties: {
        question: { type: "STRING", description: "Clear, unambiguous multiple-choice question." },
        options: {
          type: "ARRAY",
          items: { type: "STRING" },
          description: "Exactly 4 distinct answer choices."
        },
        correctIndex: {
          type: "INTEGER",
          description: "0-based index (0, 1, 2, or 3) of the correct option."
        },
        difficulty: {
          type: "INTEGER",
          description: "Difficulty tier from 1 to 4."
        },
        subTopic: { type: "STRING", description: "Specific sub-topic tested." },
        explanation: {
          type: "STRING",
          description: "Detailed explanation of why the correct option is right."
        },
        codeSnippet: {
          type: "STRING",
          description: "Optional short code or formula snippet."
        }
      },
      required: ["question", "options", "correctIndex", "difficulty", "subTopic", "explanation"]
    }
  };

  const prompt = `Generate exactly ${count} high-quality Multiple Choice Questions (MCQs) following strict Pydantic schema validation rules.
Domain Category: ${category}
Specific Topic Focus: ${topic}
Target Difficulty Tier: ${difficulty} (1=Foundation, 2=Practitioner, 3=Specialist, 4=Master)
Ensure each question has exactly 4 distinct options, a verified 0-based correctIndex (0..3), and a clear educational explanation.`;

  try {
    const candidateModels = ["gemini-2.5-flash", "gemini-1.5-flash", "gemini-1.5-pro"];
    let rawText = null;

    for (const model of candidateModels) {
      try {
        const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ role: 'user', parts: [{ text: prompt }] }],
            systemInstruction: {
              parts: [{ text: "You are a strict curriculum MCQ engine. Always return valid JSON array matching the requested schema." }]
            },
            generationConfig: {
              responseMimeType: "application/json",
              responseSchema: responseSchema,
              temperature: 0.65
            }
          })
        });

        if (response.ok) {
          const data = await response.json();
          rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text;
          if (rawText) break;
        }
      } catch (e) {
        // Continue to fallback model
      }
    }

    if (rawText) {
      const parsed = JSON.parse(rawText);
      const validated = parsed.filter(item => 
        item.question &&
        Array.isArray(item.options) &&
        item.options.length === 4 &&
        typeof item.correctIndex === 'number' &&
        item.correctIndex >= 0 &&
        item.correctIndex <= 3 &&
        item.explanation
      );

      if (validated.length > 0) {
        return res.status(200).json({
          source: 'GEMINI_AI_ONLINE',
          items: validated
        });
      }
    }

    // Fallback if AI response was empty or malformed
    const offlineItems = generateOfflineMcqs(topic, category, parseInt(difficulty, 10), parseInt(count, 10));
    return res.status(200).json({
      source: 'OFFLINE_FALLBACK',
      items: offlineItems
    });
  } catch (err) {
    const offlineItems = generateOfflineMcqs(topic, category, parseInt(difficulty, 10), parseInt(count, 10));
    return res.status(200).json({
      source: 'OFFLINE_FALLBACK_ON_ERROR',
      error: err.message,
      items: offlineItems
    });
  }
};

function generateOfflineMcqs(topic, category, difficulty, count) {
  const cleanTopic = topic || "System Architecture";
  const tier = Math.max(1, Math.min(4, difficulty || 2));
  
  const bank = [
    {
      question: `In Pydantic v2 and schema validation for '${cleanTopic}', which method serializes a model instance into a Python dictionary?`,
      options: ["model_dump()", "to_dict_schema()", "serialize_json()", "export_fields()"],
      correctIndex: 0,
      difficulty: 1,
      subTopic: cleanTopic,
      explanation: "Pydantic v2 replaced v1's .dict() with .model_dump() for dictionary conversion and .model_dump_json() for high-speed Rust-backed serialization.",
      codeSnippet: "user = UserSchema(id=42)\npayload = user.model_dump()"
    },
    {
      question: `What is the time complexity of finding shortest paths in a non-negative weighted graph with Dijkstra's algorithm and a binary min-heap?`,
      options: ["O((V + E) log V)", "O(V * E)", "O(V^2 log E)", "O(E + V)"],
      correctIndex: 0,
      difficulty: 2,
      subTopic: "Algorithms & Data Structures",
      explanation: "Extracting minimum takes O(log V) across V vertices and edge relaxation takes O(E log V), yielding O((V + E) log V).",
      codeSnippet: "priority_queue<pair<int, int>> pq;"
    },
    {
      question: `In OAuth 2.0 mobile security, what vulnerability does the PKCE (Proof Key for Code Exchange) extension prevent?`,
      options: [
        "Authorization code interception on custom redirect URI schemes",
        "Cross-site scripting (XSS) on static documentation pages",
        "SQL injection in database user tables",
        "DNS cache poisoning on local Wi-Fi networks"
      ],
      correctIndex: 0,
      difficulty: 3,
      subTopic: "Cybersecurity & OAuth 2.0",
      explanation: "PKCE binds the authorization request to token exchange with a dynamic code_verifier and code_challenge, preventing rogue apps from redeeming stolen authorization codes.",
      codeSnippet: "code_challenge = SHA256(code_verifier)"
    },
    {
      question: `According to the CAP & PACELC theorems, what trade-off does a distributed system make during normal operation (without partitions)?`,
      options: [
        "Latency (L) versus Consistency (C)",
        "Disk capacity versus CPU clock speed",
        "Memory bandwidth versus thread count",
        "Encryption strength versus packet size"
      ],
      correctIndex: 0,
      difficulty: 4,
      subTopic: "Distributed Systems",
      explanation: "PACELC states that if there is a Partition (P), trade off Availability (A) vs Consistency (C); Else (E), trade off Latency (L) vs Consistency (C).",
      codeSnippet: "PACELC: If (P) -> (A vs C); Else -> (L vs C)"
    },
    {
      question: `When building an offline-first resilient architecture for '${cleanTopic}', which pattern guarantees zero-latency state access?`,
      options: [
        "Local SQLite/Room caching as single source of truth with background sync queues",
        "Synchronous blocking HTTP requests on the main UI thread",
        "Bypassing local storage and waiting for remote websocket handshakes",
        "Discarding un-synced user actions when connectivity drops"
      ],
      correctIndex: 0,
      difficulty: tier,
      subTopic: cleanTopic,
      explanation: "An offline-first repository pattern treats local storage as the primary truth, queuing delta syncs whenever the device reconnects.",
      codeSnippet: "val data = dao.getFlow().collectAsStateWithLifecycle()"
    }
  ];

  return bank.slice(0, Math.min(count, bank.length));
}
