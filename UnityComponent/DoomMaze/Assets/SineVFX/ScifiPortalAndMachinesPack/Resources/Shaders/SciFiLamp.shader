// Shader created with Shader Forge v1.37 
// Shader Forge (c) Neat Corporation / Joachim Holmer - http://www.acegikmo.com/shaderforge/
// Note: Manually altering this data may prevent you from opening it in Shader Forge
/*SF_DATA;ver:1.37;sub:START;pass:START;ps:flbk:,iptp:0,cusa:False,bamd:0,cgin:,lico:1,lgpr:1,limd:0,spmd:1,trmd:0,grmd:0,uamb:True,mssp:True,bkdf:False,hqlp:False,rprd:False,enco:False,rmgx:True,imps:True,rpth:0,vtps:0,hqsc:True,nrmq:1,nrsp:0,vomd:0,spxs:False,tesm:0,olmd:1,culm:2,bsrc:0,bdst:0,dpts:2,wrdp:False,dith:0,atcv:False,rfrpo:True,rfrpn:Refraction,coma:15,ufog:True,aust:True,igpj:True,qofs:0,qpre:3,rntp:2,fgom:False,fgoc:True,fgod:False,fgor:False,fgmd:0,fgcr:0,fgcg:0,fgcb:0,fgca:1,fgde:0.01,fgrn:0,fgrf:300,stcl:False,stva:128,stmr:255,stmw:255,stcp:6,stps:0,stfa:0,stfz:0,ofsf:0,ofsu:0,f2p0:False,fnsp:True,fnfb:True,fsmp:False;n:type:ShaderForge.SFN_Final,id:4795,x:32845,y:32742,varname:node_4795,prsc:2|emission-7208-OUT;n:type:ShaderForge.SFN_Multiply,id:2393,x:32387,y:32875,varname:node_2393,prsc:2|A-9818-B,B-797-RGB,C-7997-OUT,D-911-OUT,E-2471-G;n:type:ShaderForge.SFN_Color,id:797,x:32120,y:32604,ptovrint:True,ptlb:Color,ptin:_TintColor,varname:_TintColor,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,c1:0.5,c2:0.5,c3:0.5,c4:1;n:type:ShaderForge.SFN_Tex2dAsset,id:3045,x:30789,y:32686,ptovrint:False,ptlb:Lamp Textures,ptin:_LampTextures,varname:node_3045,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,tex:389da78b290f1a143addab18e5c3dbb4,ntxv:0,isnm:False;n:type:ShaderForge.SFN_Tex2d,id:9818,x:32120,y:32454,varname:node_9818,prsc:2,tex:389da78b290f1a143addab18e5c3dbb4,ntxv:0,isnm:False|UVIN-9675-OUT,TEX-3045-TEX;n:type:ShaderForge.SFN_TexCoord,id:4050,x:30304,y:32346,varname:node_4050,prsc:2,uv:0,uaff:False;n:type:ShaderForge.SFN_Add,id:4094,x:31585,y:32275,varname:node_4094,prsc:2|A-8001-G,B-3896-OUT;n:type:ShaderForge.SFN_Tex2d,id:3810,x:31134,y:32310,varname:node_3810,prsc:2,tex:389da78b290f1a143addab18e5c3dbb4,ntxv:0,isnm:False|TEX-3045-TEX;n:type:ShaderForge.SFN_Multiply,id:3896,x:31356,y:32353,varname:node_3896,prsc:2|A-3810-R,B-985-OUT;n:type:ShaderForge.SFN_Slider,id:985,x:30997,y:32520,ptovrint:False,ptlb:Distortion,ptin:_Distortion,varname:node_985,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0.2564103,max:1;n:type:ShaderForge.SFN_ComponentMask,id:8001,x:31356,y:32149,varname:node_8001,prsc:2,cc1:0,cc2:1,cc3:-1,cc4:-1|IN-4050-UVOUT;n:type:ShaderForge.SFN_Append,id:9675,x:31779,y:32174,varname:node_9675,prsc:2|A-8001-R,B-4094-OUT;n:type:ShaderForge.SFN_Tex2d,id:2471,x:31655,y:32918,varname:node_2471,prsc:2,tex:389da78b290f1a143addab18e5c3dbb4,ntxv:0,isnm:False|UVIN-1312-OUT,TEX-3045-TEX;n:type:ShaderForge.SFN_Panner,id:6225,x:30946,y:32875,varname:node_6225,prsc:2,spu:0.5,spv:0|UVIN-4050-UVOUT;n:type:ShaderForge.SFN_Add,id:1312,x:31218,y:32929,varname:node_1312,prsc:2|A-6225-UVOUT,B-5166-OUT;n:type:ShaderForge.SFN_Slider,id:5166,x:30789,y:33050,ptovrint:False,ptlb:Fibers Offset,ptin:_FibersOffset,varname:node_5166,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:1;n:type:ShaderForge.SFN_Slider,id:7997,x:31963,y:32767,ptovrint:False,ptlb:Final Power,ptin:_FinalPower,varname:node_7997,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0,max:4;n:type:ShaderForge.SFN_Fresnel,id:8386,x:31950,y:32849,varname:node_8386,prsc:2;n:type:ShaderForge.SFN_OneMinus,id:911,x:32120,y:32849,varname:node_911,prsc:2|IN-8386-OUT;n:type:ShaderForge.SFN_Multiply,id:7208,x:32572,y:32967,varname:node_7208,prsc:2|A-2393-OUT,B-82-OUT;n:type:ShaderForge.SFN_Add,id:7502,x:31963,y:33150,varname:node_7502,prsc:2|A-3810-R,B-5661-OUT;n:type:ShaderForge.SFN_Clamp01,id:82,x:32139,y:33150,varname:node_82,prsc:2|IN-7502-OUT;n:type:ShaderForge.SFN_Slider,id:5661,x:31592,y:33246,ptovrint:False,ptlb:Center Contrast,ptin:_CenterContrast,varname:node_5661,prsc:2,glob:False,taghide:False,taghdr:False,tagprd:False,tagnsco:False,tagnrm:False,min:0,cur:0.5,max:1;proporder:797-3045-985-5166-7997-5661;pass:END;sub:END;*/

Shader "SineVFX/MeshPacks/SciFiLamp" {
    Properties {
        _TintColor ("Color", Color) = (0.5,0.5,0.5,1)
        _LampTextures ("Lamp Textures", 2D) = "white" {}
        _Distortion ("Distortion", Range(0, 1)) = 0.2564103
        _FibersOffset ("Fibers Offset", Range(0, 1)) = 0
        _FinalPower ("Final Power", Range(0, 4)) = 0
        _CenterContrast ("Center Contrast", Range(0, 1)) = 0.5
    }
    SubShader {
        Tags {
            "IgnoreProjector"="True"
            "Queue"="Transparent"
            "RenderType"="Transparent"
        }
        Pass {
            Name "FORWARD"
            Tags {
                "LightMode"="ForwardBase"
            }
            Blend One One
            Cull Off
            ZWrite Off
            
            CGPROGRAM
            #pragma vertex vert
            #pragma fragment frag
            #define UNITY_PASS_FORWARDBASE
            #include "UnityCG.cginc"
            #pragma multi_compile_fwdbase
            #pragma multi_compile_fog
            #pragma target 3.0
            uniform float4 _TimeEditor;
            uniform float4 _TintColor;
            uniform sampler2D _LampTextures; uniform float4 _LampTextures_ST;
            uniform float _Distortion;
            uniform float _FibersOffset;
            uniform float _FinalPower;
            uniform float _CenterContrast;
            struct VertexInput {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float2 texcoord0 : TEXCOORD0;
            };
            struct VertexOutput {
                float4 pos : SV_POSITION;
                float2 uv0 : TEXCOORD0;
                float4 posWorld : TEXCOORD1;
                float3 normalDir : TEXCOORD2;
                UNITY_FOG_COORDS(3)
            };
            VertexOutput vert (VertexInput v) {
                VertexOutput o = (VertexOutput)0;
                o.uv0 = v.texcoord0;
                o.normalDir = UnityObjectToWorldNormal(v.normal);
                o.posWorld = mul(unity_ObjectToWorld, v.vertex);
                o.pos = UnityObjectToClipPos( v.vertex );
                UNITY_TRANSFER_FOG(o,o.pos);
                return o;
            }
            float4 frag(VertexOutput i, float facing : VFACE) : COLOR {
                float isFrontFace = ( facing >= 0 ? 1 : 0 );
                float faceSign = ( facing >= 0 ? 1 : -1 );
                i.normalDir = normalize(i.normalDir);
                i.normalDir *= faceSign;
                float3 viewDirection = normalize(_WorldSpaceCameraPos.xyz - i.posWorld.xyz);
                float3 normalDirection = i.normalDir;
////// Lighting:
////// Emissive:
                float2 node_8001 = i.uv0.rg;
                float4 node_3810 = tex2D(_LampTextures,TRANSFORM_TEX(i.uv0, _LampTextures));
                float2 node_9675 = float2(node_8001.r,(node_8001.g+(node_3810.r*_Distortion)));
                float4 node_9818 = tex2D(_LampTextures,TRANSFORM_TEX(node_9675, _LampTextures));
                float4 node_8521 = _Time + _TimeEditor;
                float2 node_1312 = ((i.uv0+node_8521.g*float2(0.5,0))+_FibersOffset);
                float4 node_2471 = tex2D(_LampTextures,TRANSFORM_TEX(node_1312, _LampTextures));
                float3 emissive = ((node_9818.b*_TintColor.rgb*_FinalPower*(1.0 - (1.0-max(0,dot(normalDirection, viewDirection))))*node_2471.g)*saturate((node_3810.r+_CenterContrast)));
                float3 finalColor = emissive;
                fixed4 finalRGBA = fixed4(finalColor,1);
                UNITY_APPLY_FOG_COLOR(i.fogCoord, finalRGBA, fixed4(0,0,0,1));
                return finalRGBA;
            }
            ENDCG
        }
    }
    CustomEditor "ShaderForgeMaterialInspector"
}
