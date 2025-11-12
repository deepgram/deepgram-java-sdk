# Reference
## Agent V1 Settings Think Models
<details><summary><code>client.agent.v1.settings.think.models.list() -> AgentThinkModelsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves the available think models that can be used for AI agent processing
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().v1().settings().think().models().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Auth V1 Tokens
<details><summary><code>client.auth.v1.tokens.grant(request) -> GrantV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Generates a temporary JSON Web Token (JWT) with a 30-second (by default) TTL and usage::write permission for core voice APIs, requiring an API key with Member or higher authorization. Tokens created with this endpoint will not work with the Manage APIs.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.auth().v1().tokens().grant(
    GrantV1Request
        .builder()
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**ttlSeconds:** `Optional<Double>` — Time to live in seconds for the token. Defaults to 30 seconds.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Listen V1 Media
<details><summary><code>client.listen.v1.media.transcribeUrl(request) -> MediaTranscribeResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Transcribe audio and video using Deepgram's speech-to-text REST API
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.listen().v1().media().transcribeUrl(
    ListenV1RequestUrl
        .builder()
        .extra(
            Arrays.asList(Optional.of("extra"))
        )
        .tag(
            Arrays.asList(Optional.of("tag"))
        )
        .customTopic(
            Arrays.asList(Optional.of("custom_topic"))
        )
        .customIntent(
            Arrays.asList(Optional.of("custom_intent"))
        )
        .keywords(
            Arrays.asList(Optional.of("keywords"))
        )
        .replace(
            Arrays.asList(Optional.of("replace"))
        )
        .search(
            Arrays.asList(Optional.of("search"))
        )
        .url("https://dpgr.am/spacewalk.wav")
        .callback("callback")
        .callbackMethod(MediaTranscribeRequestCallbackMethod.POST)
        .sentiment(true)
        .summarize(MediaTranscribeRequestSummarize.V2)
        .topics(true)
        .customTopicMode(MediaTranscribeRequestCustomTopicMode.EXTENDED)
        .intents(true)
        .customIntentMode(MediaTranscribeRequestCustomIntentMode.EXTENDED)
        .detectEntities(true)
        .detectLanguage(true)
        .diarize(true)
        .dictation(true)
        .encoding(MediaTranscribeRequestEncoding.LINEAR16)
        .fillerWords(true)
        .language("language")
        .measurements(true)
        .model(MediaTranscribeRequestModel.NOVA3)
        .multichannel(true)
        .numerals(true)
        .paragraphs(true)
        .profanityFilter(true)
        .punctuate(true)
        .redact("redact")
        .smartFormat(true)
        .utterances(true)
        .uttSplit(1.1)
        .version(MediaTranscribeRequestVersion.LATEST)
        .mipOptOut(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**callback:** `Optional<String>` — URL to which we'll make the callback request
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<MediaTranscribeRequestCallbackMethod>` — HTTP method by which the callback request will be made
    
</dd>
</dl>

<dl>
<dd>

**extra:** `Optional<String>` — Arbitrary key-value pairs that are attached to the API response for usage in downstream processing
    
</dd>
</dl>

<dl>
<dd>

**sentiment:** `Optional<Boolean>` — Recognizes the sentiment throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**summarize:** `Optional<MediaTranscribeRequestSummarize>` — Summarize content. For Listen API, supports string version option. For Read API, accepts boolean only.
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Label your requests for the purpose of identification during usage reporting
    
</dd>
</dl>

<dl>
<dd>

**topics:** `Optional<Boolean>` — Detect topics throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customTopic:** `Optional<String>` — Custom topics you want the model to detect within your input audio or text if present Submit up to `100`.
    
</dd>
</dl>

<dl>
<dd>

**customTopicMode:** `Optional<MediaTranscribeRequestCustomTopicMode>` — Sets how the model will interpret strings submitted to the `custom_topic` param. When `strict`, the model will only return topics submitted using the `custom_topic` param. When `extended`, the model will return its own detected topics in addition to those submitted using the `custom_topic` param
    
</dd>
</dl>

<dl>
<dd>

**intents:** `Optional<Boolean>` — Recognizes speaker intent throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customIntent:** `Optional<String>` — Custom intents you want the model to detect within your input audio if present
    
</dd>
</dl>

<dl>
<dd>

**customIntentMode:** `Optional<MediaTranscribeRequestCustomIntentMode>` — Sets how the model will interpret intents submitted to the `custom_intent` param. When `strict`, the model will only return intents submitted using the `custom_intent` param. When `extended`, the model will return its own detected intents in the `custom_intent` param.
    
</dd>
</dl>

<dl>
<dd>

**detectEntities:** `Optional<Boolean>` — Identifies and extracts key entities from content in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**detectLanguage:** `Optional<Boolean>` — Identifies the dominant language spoken in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**diarize:** `Optional<Boolean>` — Recognize speaker changes. Each word in the transcript will be assigned a speaker number starting at 0
    
</dd>
</dl>

<dl>
<dd>

**dictation:** `Optional<Boolean>` — Dictation mode for controlling formatting with dictated speech
    
</dd>
</dl>

<dl>
<dd>

**encoding:** `Optional<MediaTranscribeRequestEncoding>` — Specify the expected encoding of your submitted audio
    
</dd>
</dl>

<dl>
<dd>

**fillerWords:** `Optional<Boolean>` — Filler Words can help transcribe interruptions in your audio, like "uh" and "um"
    
</dd>
</dl>

<dl>
<dd>

**keyterm:** `Optional<String>` — Key term prompting can boost or suppress specialized terminology and brands. Only compatible with Nova-3
    
</dd>
</dl>

<dl>
<dd>

**keywords:** `Optional<String>` — Keywords can boost or suppress specialized terminology and brands
    
</dd>
</dl>

<dl>
<dd>

**language:** `Optional<String>` — The [BCP-47 language tag](https://tools.ietf.org/html/bcp47) that hints at the primary spoken language. Depending on the Model and API endpoint you choose only certain languages are available
    
</dd>
</dl>

<dl>
<dd>

**measurements:** `Optional<Boolean>` — Spoken measurements will be converted to their corresponding abbreviations
    
</dd>
</dl>

<dl>
<dd>

**model:** `Optional<MediaTranscribeRequestModel>` — AI model used to process submitted audio
    
</dd>
</dl>

<dl>
<dd>

**multichannel:** `Optional<Boolean>` — Transcribe each audio channel independently
    
</dd>
</dl>

<dl>
<dd>

**numerals:** `Optional<Boolean>` — Numerals converts numbers from written format to numerical format
    
</dd>
</dl>

<dl>
<dd>

**paragraphs:** `Optional<Boolean>` — Splits audio into paragraphs to improve transcript readability
    
</dd>
</dl>

<dl>
<dd>

**profanityFilter:** `Optional<Boolean>` — Profanity Filter looks for recognized profanity and converts it to the nearest recognized non-profane word or removes it from the transcript completely
    
</dd>
</dl>

<dl>
<dd>

**punctuate:** `Optional<Boolean>` — Add punctuation and capitalization to the transcript
    
</dd>
</dl>

<dl>
<dd>

**redact:** `Optional<String>` — Redaction removes sensitive information from your transcripts
    
</dd>
</dl>

<dl>
<dd>

**replace:** `Optional<String>` — Search for terms or phrases in submitted audio and replaces them
    
</dd>
</dl>

<dl>
<dd>

**search:** `Optional<String>` — Search for terms or phrases in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**smartFormat:** `Optional<Boolean>` — Apply formatting to transcript output. When set to true, additional formatting will be applied to transcripts to improve readability
    
</dd>
</dl>

<dl>
<dd>

**utterances:** `Optional<Boolean>` — Segments speech into meaningful semantic units
    
</dd>
</dl>

<dl>
<dd>

**uttSplit:** `Optional<Double>` — Seconds to wait before detecting a pause between words in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<MediaTranscribeRequestVersion>` — Version of an AI model to use
    
</dd>
</dl>

<dl>
<dd>

**mipOptOut:** `Optional<Boolean>` — Opts out requests from the Deepgram Model Improvement Program. Refer to our Docs for pricing impacts before setting this to true. https://dpgr.am/deepgram-mip
    
</dd>
</dl>

<dl>
<dd>

**url:** `String` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.listen.v1.media.transcribeFile(request) -> MediaTranscribeResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Transcribe audio and video using Deepgram's speech-to-text REST API
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.listen().v1().media().transcribeFile(
    MediaTranscribeRequestOctetStream
        .builder()
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**callback:** `Optional<String>` — URL to which we'll make the callback request
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<MediaTranscribeRequestCallbackMethod>` — HTTP method by which the callback request will be made
    
</dd>
</dl>

<dl>
<dd>

**extra:** `Optional<String>` — Arbitrary key-value pairs that are attached to the API response for usage in downstream processing
    
</dd>
</dl>

<dl>
<dd>

**sentiment:** `Optional<Boolean>` — Recognizes the sentiment throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**summarize:** `Optional<MediaTranscribeRequestSummarize>` — Summarize content. For Listen API, supports string version option. For Read API, accepts boolean only.
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Label your requests for the purpose of identification during usage reporting
    
</dd>
</dl>

<dl>
<dd>

**topics:** `Optional<Boolean>` — Detect topics throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customTopic:** `Optional<String>` — Custom topics you want the model to detect within your input audio or text if present Submit up to `100`.
    
</dd>
</dl>

<dl>
<dd>

**customTopicMode:** `Optional<MediaTranscribeRequestCustomTopicMode>` — Sets how the model will interpret strings submitted to the `custom_topic` param. When `strict`, the model will only return topics submitted using the `custom_topic` param. When `extended`, the model will return its own detected topics in addition to those submitted using the `custom_topic` param
    
</dd>
</dl>

<dl>
<dd>

**intents:** `Optional<Boolean>` — Recognizes speaker intent throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customIntent:** `Optional<String>` — Custom intents you want the model to detect within your input audio if present
    
</dd>
</dl>

<dl>
<dd>

**customIntentMode:** `Optional<MediaTranscribeRequestCustomIntentMode>` — Sets how the model will interpret intents submitted to the `custom_intent` param. When `strict`, the model will only return intents submitted using the `custom_intent` param. When `extended`, the model will return its own detected intents in the `custom_intent` param.
    
</dd>
</dl>

<dl>
<dd>

**detectEntities:** `Optional<Boolean>` — Identifies and extracts key entities from content in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**detectLanguage:** `Optional<Boolean>` — Identifies the dominant language spoken in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**diarize:** `Optional<Boolean>` — Recognize speaker changes. Each word in the transcript will be assigned a speaker number starting at 0
    
</dd>
</dl>

<dl>
<dd>

**dictation:** `Optional<Boolean>` — Dictation mode for controlling formatting with dictated speech
    
</dd>
</dl>

<dl>
<dd>

**encoding:** `Optional<MediaTranscribeRequestEncoding>` — Specify the expected encoding of your submitted audio
    
</dd>
</dl>

<dl>
<dd>

**fillerWords:** `Optional<Boolean>` — Filler Words can help transcribe interruptions in your audio, like "uh" and "um"
    
</dd>
</dl>

<dl>
<dd>

**keyterm:** `Optional<String>` — Key term prompting can boost or suppress specialized terminology and brands. Only compatible with Nova-3
    
</dd>
</dl>

<dl>
<dd>

**keywords:** `Optional<String>` — Keywords can boost or suppress specialized terminology and brands
    
</dd>
</dl>

<dl>
<dd>

**language:** `Optional<String>` — The [BCP-47 language tag](https://tools.ietf.org/html/bcp47) that hints at the primary spoken language. Depending on the Model and API endpoint you choose only certain languages are available
    
</dd>
</dl>

<dl>
<dd>

**measurements:** `Optional<Boolean>` — Spoken measurements will be converted to their corresponding abbreviations
    
</dd>
</dl>

<dl>
<dd>

**model:** `Optional<MediaTranscribeRequestModel>` — AI model used to process submitted audio
    
</dd>
</dl>

<dl>
<dd>

**multichannel:** `Optional<Boolean>` — Transcribe each audio channel independently
    
</dd>
</dl>

<dl>
<dd>

**numerals:** `Optional<Boolean>` — Numerals converts numbers from written format to numerical format
    
</dd>
</dl>

<dl>
<dd>

**paragraphs:** `Optional<Boolean>` — Splits audio into paragraphs to improve transcript readability
    
</dd>
</dl>

<dl>
<dd>

**profanityFilter:** `Optional<Boolean>` — Profanity Filter looks for recognized profanity and converts it to the nearest recognized non-profane word or removes it from the transcript completely
    
</dd>
</dl>

<dl>
<dd>

**punctuate:** `Optional<Boolean>` — Add punctuation and capitalization to the transcript
    
</dd>
</dl>

<dl>
<dd>

**redact:** `Optional<String>` — Redaction removes sensitive information from your transcripts
    
</dd>
</dl>

<dl>
<dd>

**replace:** `Optional<String>` — Search for terms or phrases in submitted audio and replaces them
    
</dd>
</dl>

<dl>
<dd>

**search:** `Optional<String>` — Search for terms or phrases in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**smartFormat:** `Optional<Boolean>` — Apply formatting to transcript output. When set to true, additional formatting will be applied to transcripts to improve readability
    
</dd>
</dl>

<dl>
<dd>

**utterances:** `Optional<Boolean>` — Segments speech into meaningful semantic units
    
</dd>
</dl>

<dl>
<dd>

**uttSplit:** `Optional<Double>` — Seconds to wait before detecting a pause between words in submitted audio
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<MediaTranscribeRequestVersion>` — Version of an AI model to use
    
</dd>
</dl>

<dl>
<dd>

**mipOptOut:** `Optional<Boolean>` — Opts out requests from the Deepgram Model Improvement Program. Refer to our Docs for pricing impacts before setting this to true. https://dpgr.am/deepgram-mip
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Models
<details><summary><code>client.manage.v1.models.list() -> ListModelsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns metadata on all the latest public models. To retrieve custom models, use Get Project Models.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().models().list(
    ModelsListRequest
        .builder()
        .includeOutdated(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**includeOutdated:** `Optional<Boolean>` — returns non-latest versions of models
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.models.get(modelId) -> GetModelV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns metadata for a specific public model
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().models().get("af6e9977-99f6-4d8f-b6f5-dfdf6fb6e291");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**modelId:** `String` — The specific UUID of the model
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects
<details><summary><code>client.manage.v1.projects.list() -> ListProjectsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves basic information about the projects associated with the API key
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.get(projectId) -> GetProjectV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves information about the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().get(
    "123456-7890-1234-5678-901234",
    ProjectsGetRequest
        .builder()
        .limit(1.1)
        .page(1.1)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Double>` — Number of results to return per page. Default 10. Range [1,1000]
    
</dd>
</dl>

<dl>
<dd>

**page:** `Optional<Double>` — Navigate and return the results to retrieve specific portions of information of the response
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.delete(projectId) -> DeleteProjectV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().delete("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.update(projectId, request) -> UpdateProjectV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates the name or other properties of an existing project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().update(
    "123456-7890-1234-5678-901234",
    UpdateProjectV1Request
        .builder()
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**name:** `Optional<String>` — The name of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.leave(projectId) -> LeaveProjectV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Removes the authenticated account from the specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().leave("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Keys
<details><summary><code>client.manage.v1.projects.keys.list(projectId) -> ListProjectKeysV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves all API keys associated with the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().keys().list(
    "123456-7890-1234-5678-901234",
    KeysListRequest
        .builder()
        .status(KeysListRequestStatus.ACTIVE)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**status:** `Optional<KeysListRequestStatus>` — Only return keys with a specific status
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.keys.create(projectId, request) -> CreateKeyV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new API key with specified settings for the project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().keys().create("project_id", new 
HashMap<String, Object>() {{put("key", "value");
}});
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**request:** `Object` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.keys.get(projectId, keyId) -> GetProjectKeyV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves information about a specified API key
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().keys().get("123456-7890-1234-5678-901234", "123456789012345678901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**keyId:** `String` — The unique identifier of the API key
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.keys.delete(projectId, keyId) -> DeleteProjectKeyV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes an API key for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().keys().delete("123456-7890-1234-5678-901234", "123456789012345678901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**keyId:** `String` — The unique identifier of the API key
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Members
<details><summary><code>client.manage.v1.projects.members.list(projectId) -> ListProjectMembersV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of members for a given project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().list("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.members.delete(projectId, memberId) -> DeleteProjectMemberV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Removes a member from the project using their unique member ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().delete("123456-7890-1234-5678-901234", "123456789012345678901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**memberId:** `String` — The unique identifier of the Member
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Models
<details><summary><code>client.manage.v1.projects.models.list(projectId) -> ListModelsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns metadata on all the latest models that a specific project has access to, including non-public models
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().models().list(
    "123456-7890-1234-5678-901234",
    ModelsListRequest
        .builder()
        .includeOutdated(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**includeOutdated:** `Optional<Boolean>` — returns non-latest versions of models
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.models.get(projectId, modelId) -> GetModelV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns metadata for a specific model
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().models().get("123456-7890-1234-5678-901234", "af6e9977-99f6-4d8f-b6f5-dfdf6fb6e291");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**modelId:** `String` — The specific UUID of the model
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Requests
<details><summary><code>client.manage.v1.projects.requests.list(projectId) -> ListProjectRequestsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Generates a list of requests for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().requests().list(
    "123456-7890-1234-5678-901234",
    RequestsListRequest
        .builder()
        .start(OffsetDateTime.parse("2024-01-15T09:30:00Z"))
        .end(OffsetDateTime.parse("2024-01-15T09:30:00Z"))
        .limit(1.1)
        .page(1.1)
        .accessor("12345678-1234-1234-1234-123456789012")
        .requestId("12345678-1234-1234-1234-123456789012")
        .deployment(RequestsListRequestDeployment.HOSTED)
        .endpoint(RequestsListRequestEndpoint.LISTEN)
        .method(RequestsListRequestMethod.SYNC)
        .status(RequestsListRequestStatus.SUCCEEDED)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<OffsetDateTime>` — Start date of the requested date range. Formats accepted are YYYY-MM-DD, YYYY-MM-DDTHH:MM:SS, or YYYY-MM-DDTHH:MM:SS+HH:MM
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<OffsetDateTime>` — End date of the requested date range. Formats accepted are YYYY-MM-DD, YYYY-MM-DDTHH:MM:SS, or YYYY-MM-DDTHH:MM:SS+HH:MM
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Double>` — Number of results to return per page. Default 10. Range [1,1000]
    
</dd>
</dl>

<dl>
<dd>

**page:** `Optional<Double>` — Navigate and return the results to retrieve specific portions of information of the response
    
</dd>
</dl>

<dl>
<dd>

**accessor:** `Optional<String>` — Filter for requests where a specific accessor was used
    
</dd>
</dl>

<dl>
<dd>

**requestId:** `Optional<String>` — Filter for a specific request id
    
</dd>
</dl>

<dl>
<dd>

**deployment:** `Optional<RequestsListRequestDeployment>` — Filter for requests where a specific deployment was used
    
</dd>
</dl>

<dl>
<dd>

**endpoint:** `Optional<RequestsListRequestEndpoint>` — Filter for requests where a specific endpoint was used
    
</dd>
</dl>

<dl>
<dd>

**method:** `Optional<RequestsListRequestMethod>` — Filter for requests where a specific method was used
    
</dd>
</dl>

<dl>
<dd>

**status:** `Optional<RequestsListRequestStatus>` — Filter for requests that succeeded (status code < 300) or failed (status code >=400)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.requests.get(projectId, requestId) -> GetProjectRequestV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a specific request for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().requests().get("123456-7890-1234-5678-901234", "123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**requestId:** `String` — The unique identifier of the request
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Usage
<details><summary><code>client.manage.v1.projects.usage.get(projectId) -> UsageV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves the usage for a specific project. Use Get Project Usage Breakdown for a more comprehensive usage summary.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().usage().get(
    "123456-7890-1234-5678-901234",
    UsageGetRequest
        .builder()
        .start("start")
        .end("end")
        .accessor("12345678-1234-1234-1234-123456789012")
        .alternatives(true)
        .callbackMethod(true)
        .callback(true)
        .channels(true)
        .customIntentMode(true)
        .customIntent(true)
        .customTopicMode(true)
        .customTopic(true)
        .deployment(UsageGetRequestDeployment.HOSTED)
        .detectEntities(true)
        .detectLanguage(true)
        .diarize(true)
        .dictation(true)
        .encoding(true)
        .endpoint(UsageGetRequestEndpoint.LISTEN)
        .extra(true)
        .fillerWords(true)
        .intents(true)
        .keyterm(true)
        .keywords(true)
        .language(true)
        .measurements(true)
        .method(UsageGetRequestMethod.SYNC)
        .model("6f548761-c9c0-429a-9315-11a1d28499c8")
        .multichannel(true)
        .numerals(true)
        .paragraphs(true)
        .profanityFilter(true)
        .punctuate(true)
        .redact(true)
        .replace(true)
        .sampleRate(true)
        .search(true)
        .sentiment(true)
        .smartFormat(true)
        .summarize(true)
        .tag("tag1")
        .topics(true)
        .uttSplit(true)
        .utterances(true)
        .version(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<String>` — Start date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<String>` — End date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**accessor:** `Optional<String>` — Filter for requests where a specific accessor was used
    
</dd>
</dl>

<dl>
<dd>

**alternatives:** `Optional<Boolean>` — Filter for requests where alternatives were used
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<Boolean>` — Filter for requests where callback method was used
    
</dd>
</dl>

<dl>
<dd>

**callback:** `Optional<Boolean>` — Filter for requests where callback was used
    
</dd>
</dl>

<dl>
<dd>

**channels:** `Optional<Boolean>` — Filter for requests where channels were used
    
</dd>
</dl>

<dl>
<dd>

**customIntentMode:** `Optional<Boolean>` — Filter for requests where custom intent mode was used
    
</dd>
</dl>

<dl>
<dd>

**customIntent:** `Optional<Boolean>` — Filter for requests where custom intent was used
    
</dd>
</dl>

<dl>
<dd>

**customTopicMode:** `Optional<Boolean>` — Filter for requests where custom topic mode was used
    
</dd>
</dl>

<dl>
<dd>

**customTopic:** `Optional<Boolean>` — Filter for requests where custom topic was used
    
</dd>
</dl>

<dl>
<dd>

**deployment:** `Optional<UsageGetRequestDeployment>` — Filter for requests where a specific deployment was used
    
</dd>
</dl>

<dl>
<dd>

**detectEntities:** `Optional<Boolean>` — Filter for requests where detect entities was used
    
</dd>
</dl>

<dl>
<dd>

**detectLanguage:** `Optional<Boolean>` — Filter for requests where detect language was used
    
</dd>
</dl>

<dl>
<dd>

**diarize:** `Optional<Boolean>` — Filter for requests where diarize was used
    
</dd>
</dl>

<dl>
<dd>

**dictation:** `Optional<Boolean>` — Filter for requests where dictation was used
    
</dd>
</dl>

<dl>
<dd>

**encoding:** `Optional<Boolean>` — Filter for requests where encoding was used
    
</dd>
</dl>

<dl>
<dd>

**endpoint:** `Optional<UsageGetRequestEndpoint>` — Filter for requests where a specific endpoint was used
    
</dd>
</dl>

<dl>
<dd>

**extra:** `Optional<Boolean>` — Filter for requests where extra was used
    
</dd>
</dl>

<dl>
<dd>

**fillerWords:** `Optional<Boolean>` — Filter for requests where filler words was used
    
</dd>
</dl>

<dl>
<dd>

**intents:** `Optional<Boolean>` — Filter for requests where intents was used
    
</dd>
</dl>

<dl>
<dd>

**keyterm:** `Optional<Boolean>` — Filter for requests where keyterm was used
    
</dd>
</dl>

<dl>
<dd>

**keywords:** `Optional<Boolean>` — Filter for requests where keywords was used
    
</dd>
</dl>

<dl>
<dd>

**language:** `Optional<Boolean>` — Filter for requests where language was used
    
</dd>
</dl>

<dl>
<dd>

**measurements:** `Optional<Boolean>` — Filter for requests where measurements were used
    
</dd>
</dl>

<dl>
<dd>

**method:** `Optional<UsageGetRequestMethod>` — Filter for requests where a specific method was used
    
</dd>
</dl>

<dl>
<dd>

**model:** `Optional<String>` — Filter for requests where a specific model uuid was used
    
</dd>
</dl>

<dl>
<dd>

**multichannel:** `Optional<Boolean>` — Filter for requests where multichannel was used
    
</dd>
</dl>

<dl>
<dd>

**numerals:** `Optional<Boolean>` — Filter for requests where numerals were used
    
</dd>
</dl>

<dl>
<dd>

**paragraphs:** `Optional<Boolean>` — Filter for requests where paragraphs were used
    
</dd>
</dl>

<dl>
<dd>

**profanityFilter:** `Optional<Boolean>` — Filter for requests where profanity filter was used
    
</dd>
</dl>

<dl>
<dd>

**punctuate:** `Optional<Boolean>` — Filter for requests where punctuate was used
    
</dd>
</dl>

<dl>
<dd>

**redact:** `Optional<Boolean>` — Filter for requests where redact was used
    
</dd>
</dl>

<dl>
<dd>

**replace:** `Optional<Boolean>` — Filter for requests where replace was used
    
</dd>
</dl>

<dl>
<dd>

**sampleRate:** `Optional<Boolean>` — Filter for requests where sample rate was used
    
</dd>
</dl>

<dl>
<dd>

**search:** `Optional<Boolean>` — Filter for requests where search was used
    
</dd>
</dl>

<dl>
<dd>

**sentiment:** `Optional<Boolean>` — Filter for requests where sentiment was used
    
</dd>
</dl>

<dl>
<dd>

**smartFormat:** `Optional<Boolean>` — Filter for requests where smart format was used
    
</dd>
</dl>

<dl>
<dd>

**summarize:** `Optional<Boolean>` — Filter for requests where summarize was used
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Filter for requests where a specific tag was used
    
</dd>
</dl>

<dl>
<dd>

**topics:** `Optional<Boolean>` — Filter for requests where topics was used
    
</dd>
</dl>

<dl>
<dd>

**uttSplit:** `Optional<Boolean>` — Filter for requests where utt split was used
    
</dd>
</dl>

<dl>
<dd>

**utterances:** `Optional<Boolean>` — Filter for requests where utterances was used
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<Boolean>` — Filter for requests where version was used
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Billing Balances
<details><summary><code>client.manage.v1.projects.billing.balances.list(projectId) -> ListProjectBalancesV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Generates a list of outstanding balances for the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().billing().balances().list("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.billing.balances.get(projectId, balanceId) -> GetProjectBalanceV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves details about the specified balance
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().billing().balances().get("123456-7890-1234-5678-901234", "123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**balanceId:** `String` — The unique identifier of the balance
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Billing Breakdown
<details><summary><code>client.manage.v1.projects.billing.breakdown.list(projectId) -> BillingBreakdownV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves the billing summary for a specific project, with various filter options or by grouping options.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().billing().breakdown().list(
    "123456-7890-1234-5678-901234",
    BreakdownListRequest
        .builder()
        .start("start")
        .end("end")
        .accessor("12345678-1234-1234-1234-123456789012")
        .deployment(BreakdownListRequestDeployment.HOSTED)
        .tag("tag1")
        .lineItem("streaming::nova-3")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<String>` — Start date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<String>` — End date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**accessor:** `Optional<String>` — Filter for requests where a specific accessor was used
    
</dd>
</dl>

<dl>
<dd>

**deployment:** `Optional<BreakdownListRequestDeployment>` — Filter for requests where a specific deployment was used
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Filter for requests where a specific tag was used
    
</dd>
</dl>

<dl>
<dd>

**lineItem:** `Optional<String>` — Filter requests by line item (e.g. streaming::nova-3)
    
</dd>
</dl>

<dl>
<dd>

**grouping:** `Optional<BreakdownListRequestGroupingItem>` — Group billing breakdown by one or more dimensions (accessor, deployment, line_item, tags)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Billing Fields
<details><summary><code>client.manage.v1.projects.billing.fields.list(projectId) -> ListBillingFieldsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Lists the accessors, deployment types, tags, and line items used for billing data in the specified time period. Use this endpoint if you want to filter your results from the Billing Breakdown endpoint and want to know what filters are available.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().billing().fields().list(
    "123456-7890-1234-5678-901234",
    FieldsListRequest
        .builder()
        .start("start")
        .end("end")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<String>` — Start date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<String>` — End date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Billing Purchases
<details><summary><code>client.manage.v1.projects.billing.purchases.list(projectId) -> ListProjectPurchasesV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns the original purchased amount on an order transaction
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().billing().purchases().list(
    "123456-7890-1234-5678-901234",
    PurchasesListRequest
        .builder()
        .limit(1.1)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Double>` — Number of results to return per page. Default 10. Range [1,1000]
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Members Invites
<details><summary><code>client.manage.v1.projects.members.invites.list(projectId) -> ListProjectInvitesV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Generates a list of invites for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().invites().list("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.members.invites.create(projectId, request) -> CreateProjectInviteV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Generates an invite for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().invites().create(
    "123456-7890-1234-5678-901234",
    CreateProjectInviteV1Request
        .builder()
        .email("email")
        .scope("scope")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**email:** `String` — The email address of the invitee
    
</dd>
</dl>

<dl>
<dd>

**scope:** `String` — The scope of the invitee
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.members.invites.delete(projectId, email) -> DeleteProjectInviteV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes an invite for a specific project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().invites().delete("123456-7890-1234-5678-901234", "john.doe@example.com");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**email:** `String` — The email address of the member
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Members Scopes
<details><summary><code>client.manage.v1.projects.members.scopes.list(projectId, memberId) -> ListProjectMemberScopesV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of scopes for a specific member
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().scopes().list("123456-7890-1234-5678-901234", "123456789012345678901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**memberId:** `String` — The unique identifier of the Member
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.manage.v1.projects.members.scopes.update(projectId, memberId, request) -> UpdateProjectMemberScopesV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates the scopes for a specific member
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().members().scopes().update(
    "123456-7890-1234-5678-901234",
    "123456789012345678901234",
    UpdateProjectMemberScopesV1Request
        .builder()
        .scope("admin")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**memberId:** `String` — The unique identifier of the Member
    
</dd>
</dl>

<dl>
<dd>

**scope:** `String` — A scope to update
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Usage Breakdown
<details><summary><code>client.manage.v1.projects.usage.breakdown.get(projectId) -> UsageBreakdownV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves the usage breakdown for a specific project, with various filter options by API feature or by groupings. Setting a feature (e.g. diarize) to true includes requests that used that feature, while false excludes requests that used it. Multiple true filters are combined with OR logic, while false filters use AND logic.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().usage().breakdown().get(
    "123456-7890-1234-5678-901234",
    BreakdownGetRequest
        .builder()
        .start("start")
        .end("end")
        .grouping(BreakdownGetRequestGrouping.ACCESSOR)
        .accessor("12345678-1234-1234-1234-123456789012")
        .alternatives(true)
        .callbackMethod(true)
        .callback(true)
        .channels(true)
        .customIntentMode(true)
        .customIntent(true)
        .customTopicMode(true)
        .customTopic(true)
        .deployment(BreakdownGetRequestDeployment.HOSTED)
        .detectEntities(true)
        .detectLanguage(true)
        .diarize(true)
        .dictation(true)
        .encoding(true)
        .endpoint(BreakdownGetRequestEndpoint.LISTEN)
        .extra(true)
        .fillerWords(true)
        .intents(true)
        .keyterm(true)
        .keywords(true)
        .language(true)
        .measurements(true)
        .method(BreakdownGetRequestMethod.SYNC)
        .model("6f548761-c9c0-429a-9315-11a1d28499c8")
        .multichannel(true)
        .numerals(true)
        .paragraphs(true)
        .profanityFilter(true)
        .punctuate(true)
        .redact(true)
        .replace(true)
        .sampleRate(true)
        .search(true)
        .sentiment(true)
        .smartFormat(true)
        .summarize(true)
        .tag("tag1")
        .topics(true)
        .uttSplit(true)
        .utterances(true)
        .version(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<String>` — Start date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<String>` — End date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**grouping:** `Optional<BreakdownGetRequestGrouping>` — Common usage grouping parameters
    
</dd>
</dl>

<dl>
<dd>

**accessor:** `Optional<String>` — Filter for requests where a specific accessor was used
    
</dd>
</dl>

<dl>
<dd>

**alternatives:** `Optional<Boolean>` — Filter for requests where alternatives were used
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<Boolean>` — Filter for requests where callback method was used
    
</dd>
</dl>

<dl>
<dd>

**callback:** `Optional<Boolean>` — Filter for requests where callback was used
    
</dd>
</dl>

<dl>
<dd>

**channels:** `Optional<Boolean>` — Filter for requests where channels were used
    
</dd>
</dl>

<dl>
<dd>

**customIntentMode:** `Optional<Boolean>` — Filter for requests where custom intent mode was used
    
</dd>
</dl>

<dl>
<dd>

**customIntent:** `Optional<Boolean>` — Filter for requests where custom intent was used
    
</dd>
</dl>

<dl>
<dd>

**customTopicMode:** `Optional<Boolean>` — Filter for requests where custom topic mode was used
    
</dd>
</dl>

<dl>
<dd>

**customTopic:** `Optional<Boolean>` — Filter for requests where custom topic was used
    
</dd>
</dl>

<dl>
<dd>

**deployment:** `Optional<BreakdownGetRequestDeployment>` — Filter for requests where a specific deployment was used
    
</dd>
</dl>

<dl>
<dd>

**detectEntities:** `Optional<Boolean>` — Filter for requests where detect entities was used
    
</dd>
</dl>

<dl>
<dd>

**detectLanguage:** `Optional<Boolean>` — Filter for requests where detect language was used
    
</dd>
</dl>

<dl>
<dd>

**diarize:** `Optional<Boolean>` — Filter for requests where diarize was used
    
</dd>
</dl>

<dl>
<dd>

**dictation:** `Optional<Boolean>` — Filter for requests where dictation was used
    
</dd>
</dl>

<dl>
<dd>

**encoding:** `Optional<Boolean>` — Filter for requests where encoding was used
    
</dd>
</dl>

<dl>
<dd>

**endpoint:** `Optional<BreakdownGetRequestEndpoint>` — Filter for requests where a specific endpoint was used
    
</dd>
</dl>

<dl>
<dd>

**extra:** `Optional<Boolean>` — Filter for requests where extra was used
    
</dd>
</dl>

<dl>
<dd>

**fillerWords:** `Optional<Boolean>` — Filter for requests where filler words was used
    
</dd>
</dl>

<dl>
<dd>

**intents:** `Optional<Boolean>` — Filter for requests where intents was used
    
</dd>
</dl>

<dl>
<dd>

**keyterm:** `Optional<Boolean>` — Filter for requests where keyterm was used
    
</dd>
</dl>

<dl>
<dd>

**keywords:** `Optional<Boolean>` — Filter for requests where keywords was used
    
</dd>
</dl>

<dl>
<dd>

**language:** `Optional<Boolean>` — Filter for requests where language was used
    
</dd>
</dl>

<dl>
<dd>

**measurements:** `Optional<Boolean>` — Filter for requests where measurements were used
    
</dd>
</dl>

<dl>
<dd>

**method:** `Optional<BreakdownGetRequestMethod>` — Filter for requests where a specific method was used
    
</dd>
</dl>

<dl>
<dd>

**model:** `Optional<String>` — Filter for requests where a specific model uuid was used
    
</dd>
</dl>

<dl>
<dd>

**multichannel:** `Optional<Boolean>` — Filter for requests where multichannel was used
    
</dd>
</dl>

<dl>
<dd>

**numerals:** `Optional<Boolean>` — Filter for requests where numerals were used
    
</dd>
</dl>

<dl>
<dd>

**paragraphs:** `Optional<Boolean>` — Filter for requests where paragraphs were used
    
</dd>
</dl>

<dl>
<dd>

**profanityFilter:** `Optional<Boolean>` — Filter for requests where profanity filter was used
    
</dd>
</dl>

<dl>
<dd>

**punctuate:** `Optional<Boolean>` — Filter for requests where punctuate was used
    
</dd>
</dl>

<dl>
<dd>

**redact:** `Optional<Boolean>` — Filter for requests where redact was used
    
</dd>
</dl>

<dl>
<dd>

**replace:** `Optional<Boolean>` — Filter for requests where replace was used
    
</dd>
</dl>

<dl>
<dd>

**sampleRate:** `Optional<Boolean>` — Filter for requests where sample rate was used
    
</dd>
</dl>

<dl>
<dd>

**search:** `Optional<Boolean>` — Filter for requests where search was used
    
</dd>
</dl>

<dl>
<dd>

**sentiment:** `Optional<Boolean>` — Filter for requests where sentiment was used
    
</dd>
</dl>

<dl>
<dd>

**smartFormat:** `Optional<Boolean>` — Filter for requests where smart format was used
    
</dd>
</dl>

<dl>
<dd>

**summarize:** `Optional<Boolean>` — Filter for requests where summarize was used
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Filter for requests where a specific tag was used
    
</dd>
</dl>

<dl>
<dd>

**topics:** `Optional<Boolean>` — Filter for requests where topics was used
    
</dd>
</dl>

<dl>
<dd>

**uttSplit:** `Optional<Boolean>` — Filter for requests where utt split was used
    
</dd>
</dl>

<dl>
<dd>

**utterances:** `Optional<Boolean>` — Filter for requests where utterances was used
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<Boolean>` — Filter for requests where version was used
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Manage V1 Projects Usage Fields
<details><summary><code>client.manage.v1.projects.usage.fields.list(projectId) -> UsageFieldsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Lists the features, models, tags, languages, and processing method used for requests in the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.manage().v1().projects().usage().fields().list(
    "123456-7890-1234-5678-901234",
    FieldsListRequest
        .builder()
        .start("start")
        .end("end")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**start:** `Optional<String>` — Start date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>

<dl>
<dd>

**end:** `Optional<String>` — End date of the requested date range. Format accepted is YYYY-MM-DD
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Read V1 Text
<details><summary><code>client.read.v1.text.analyze(request) -> ReadV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Analyze text content using Deepgrams text analysis API
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.read().v1().text().analyze(
    TextAnalyzeRequest
        .builder()
        .tag(
            Arrays.asList(Optional.of("tag"))
        )
        .customTopic(
            Arrays.asList(Optional.of("custom_topic"))
        )
        .customIntent(
            Arrays.asList(Optional.of("custom_intent"))
        )
        .body(
            ReadV1Request.of(
                ReadV1RequestUrl
                    .builder()
                    .url("url")
                    .build()
            )
        )
        .callback("callback")
        .callbackMethod(TextAnalyzeRequestCallbackMethod.POST)
        .sentiment(true)
        .summarize(TextAnalyzeRequestSummarize.V2)
        .topics(true)
        .customTopicMode(TextAnalyzeRequestCustomTopicMode.EXTENDED)
        .intents(true)
        .customIntentMode(TextAnalyzeRequestCustomIntentMode.EXTENDED)
        .language("language")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**callback:** `Optional<String>` — URL to which we'll make the callback request
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<TextAnalyzeRequestCallbackMethod>` — HTTP method by which the callback request will be made
    
</dd>
</dl>

<dl>
<dd>

**sentiment:** `Optional<Boolean>` — Recognizes the sentiment throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**summarize:** `Optional<TextAnalyzeRequestSummarize>` — Summarize content. For Listen API, supports string version option. For Read API, accepts boolean only.
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Label your requests for the purpose of identification during usage reporting
    
</dd>
</dl>

<dl>
<dd>

**topics:** `Optional<Boolean>` — Detect topics throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customTopic:** `Optional<String>` — Custom topics you want the model to detect within your input audio or text if present Submit up to `100`.
    
</dd>
</dl>

<dl>
<dd>

**customTopicMode:** `Optional<TextAnalyzeRequestCustomTopicMode>` — Sets how the model will interpret strings submitted to the `custom_topic` param. When `strict`, the model will only return topics submitted using the `custom_topic` param. When `extended`, the model will return its own detected topics in addition to those submitted using the `custom_topic` param
    
</dd>
</dl>

<dl>
<dd>

**intents:** `Optional<Boolean>` — Recognizes speaker intent throughout a transcript or text
    
</dd>
</dl>

<dl>
<dd>

**customIntent:** `Optional<String>` — Custom intents you want the model to detect within your input audio if present
    
</dd>
</dl>

<dl>
<dd>

**customIntentMode:** `Optional<TextAnalyzeRequestCustomIntentMode>` — Sets how the model will interpret intents submitted to the `custom_intent` param. When `strict`, the model will only return intents submitted using the `custom_intent` param. When `extended`, the model will return its own detected intents in the `custom_intent` param.
    
</dd>
</dl>

<dl>
<dd>

**language:** `Optional<String>` — The [BCP-47 language tag](https://tools.ietf.org/html/bcp47) that hints at the primary spoken language. Depending on the Model and API endpoint you choose only certain languages are available
    
</dd>
</dl>

<dl>
<dd>

**request:** `ReadV1Request` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## SelfHosted V1 DistributionCredentials
<details><summary><code>client.selfHosted.v1.distributionCredentials.list(projectId) -> ListProjectDistributionCredentialsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Lists sets of distribution credentials for the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.selfHosted().v1().distributionCredentials().list("123456-7890-1234-5678-901234");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.selfHosted.v1.distributionCredentials.create(projectId, request) -> CreateProjectDistributionCredentialsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a set of distribution credentials for the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.selfHosted().v1().distributionCredentials().create(
    "123456-7890-1234-5678-901234",
    CreateProjectDistributionCredentialsV1Request
        .builder()
        .provider("quay")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**scopes:** `Optional<DistributionCredentialsCreateRequestScopesItem>` — List of permission scopes for the credentials
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<String>` — The provider of the distribution service
    
</dd>
</dl>

<dl>
<dd>

**comment:** `Optional<String>` — Optional comment about the credentials
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.selfHosted.v1.distributionCredentials.get(projectId, distributionCredentialsId) -> GetProjectDistributionCredentialsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns a set of distribution credentials for the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.selfHosted().v1().distributionCredentials().get("123456-7890-1234-5678-901234", "8b36cfd0-472f-4a21-833f-2d6343c3a2f3");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**distributionCredentialsId:** `String` — The UUID of the distribution credentials
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.selfHosted.v1.distributionCredentials.delete(projectId, distributionCredentialsId) -> GetProjectDistributionCredentialsV1Response</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a set of distribution credentials for the specified project
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.selfHosted().v1().distributionCredentials().delete("123456-7890-1234-5678-901234", "8b36cfd0-472f-4a21-833f-2d6343c3a2f3");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**projectId:** `String` — The unique identifier of the project
    
</dd>
</dl>

<dl>
<dd>

**distributionCredentialsId:** `String` — The UUID of the distribution credentials
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Speak V1 Audio
<details><summary><code>client.speak.v1.audio.generate(request) -> InputStream</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Convert text into natural-sounding speech using Deepgram's TTS REST API
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.speak().v1().audio().generate(
    SpeakV1Request
        .builder()
        .text("text")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**callback:** `Optional<String>` — URL to which we'll make the callback request
    
</dd>
</dl>

<dl>
<dd>

**callbackMethod:** `Optional<AudioGenerateRequestCallbackMethod>` — HTTP method by which the callback request will be made
    
</dd>
</dl>

<dl>
<dd>

**mipOptOut:** `Optional<Boolean>` — Opts out requests from the Deepgram Model Improvement Program. Refer to our Docs for pricing impacts before setting this to true. https://dpgr.am/deepgram-mip
    
</dd>
</dl>

<dl>
<dd>

**tag:** `Optional<String>` — Label your requests for the purpose of identification during usage reporting
    
</dd>
</dl>

<dl>
<dd>

**bitRate:** `Optional<Double>` — The bitrate of the audio in bits per second. Choose from predefined ranges or specific values based on the encoding type.
    
</dd>
</dl>

<dl>
<dd>

**container:** `Optional<AudioGenerateRequestContainer>` — Container specifies the file format wrapper for the output audio. The available options depend on the encoding type.
    
</dd>
</dl>

<dl>
<dd>

**encoding:** `Optional<AudioGenerateRequestEncoding>` — Encoding allows you to specify the expected encoding of your audio output
    
</dd>
</dl>

<dl>
<dd>

**model:** `Optional<AudioGenerateRequestModel>` — AI model used to process submitted text
    
</dd>
</dl>

<dl>
<dd>

**sampleRate:** `Optional<Double>` — Sample Rate specifies the sample rate for the output audio. Based on the encoding, different sample rates are supported. For some encodings, the sample rate is not configurable
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — The text content to be converted to speech
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>
