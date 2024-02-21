// SonarQube IaC Plugin
// Copyright (C) 2021-2024 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"github.com/BurntSushi/toml"
	"github.com/Masterminds/sprig/v3"
	"github.com/sonarsource/template"
	"sigs.k8s.io/yaml"
	"slices"
	"strings"
)

// This is a list of functions that Helm imports from Sprig. It is defined in pkg/engine/funcs.go in Helm and calls sprig.TxtFuncMap(), removing
// only some of the functions (`env`, `expandenv`) and overriding some of the others (`toJson`, `fromJson`, `fail`, `getHostByName`).
var sprigFunctionsWhitelist = []string{
	"abbrev",
	"abbrevboth",
	"add",
	"add1",
	"add1f",
	"addf",
	"adler32sum",
	"ago",
	"all",
	"any",
	"append",
	"atoi",
	"b32dec",
	"b32enc",
	"b64dec",
	"b64enc",
	"base",
	"bcrypt",
	"biggest",
	"buildCustomCert",
	"camelcase",
	"cat",
	"ceil",
	"chunk",
	"clean",
	"coalesce",
	"compact",
	"concat",
	"contains",
	"date",
	"date_in_zone",
	"dateInZone",
	"date_modify",
	"dateModify",
	"decryptAES",
	"deepCopy",
	"deepEqual",
	"default",
	"derivePassword",
	"dict",
	"dig",
	"dir",
	"div",
	"divf",
	"duration",
	"durationRound",
	"empty",
	"encryptAES",
	"ext",
	"fail",
	"first",
	"float64",
	"floor",
	"genCA",
	"genCAWithKey",
	"genPrivateKey",
	"genSelfSignedCert",
	"genSelfSignedCertWithKey",
	"genSignedCert",
	"genSignedCertWithKey",
	"get",
	"has",
	"hasKey",
	"hasPrefix",
	"hasSuffix",
	"hello",
	"htmlDate",
	"htmlDateInZone",
	"htpasswd",
	"indent",
	"initial",
	"initials",
	"int",
	"int64",
	"isAbs",
	"join",
	"kebabcase",
	"keys",
	"kindIs",
	"kindOf",
	"last",
	"list",
	"lower",
	"max",
	"maxf",
	"merge",
	"mergeOverwrite",
	"min",
	"minf",
	"mod",
	"mul",
	"mulf",
	"mustAppend",
	"mustChunk",
	"mustCompact",
	"must_date_modify",
	"mustDateModify",
	"mustDeepCopy",
	"mustFirst",
	"mustFromJson",
	"mustHas",
	"mustInitial",
	"mustLast",
	"mustMerge",
	"mustMergeOverwrite",
	"mustPrepend",
	"mustRegexFind",
	"mustRegexFindAll",
	"mustRegexMatch",
	"mustRegexReplaceAll",
	"mustRegexReplaceAllLiteral",
	"mustRegexSplit",
	"mustRest",
	"mustReverse",
	"mustSlice",
	"mustToDate",
	"mustToJson",
	"mustToPrettyJson",
	"mustToRawJson",
	"mustUniq",
	"mustWithout",
	"nindent",
	"nospace",
	"now",
	"omit",
	"osBase",
	"osClean",
	"osDir",
	"osExt",
	"osIsAbs",
	"pick",
	"pluck",
	"plural",
	"prepend",
	"quote",
	"randAlpha",
	"randAlphaNum",
	"randAscii",
	"randBytes",
	"randInt",
	"randNumeric",
	"regexFind",
	"regexFindAll",
	"regexMatch",
	"regexQuoteMeta",
	"regexReplaceAll",
	"regexReplaceAllLiteral",
	"regexSplit",
	"repeat",
	"replace",
	"rest",
	"reverse",
	"round",
	"semver",
	"semverCompare",
	"seq",
	"set",
	"sha1sum",
	"sha256sum",
	"shuffle",
	"slice",
	"snakecase",
	"sortAlpha",
	"split",
	"splitList",
	"splitn",
	"squote",
	"sub",
	"subf",
	"substr",
	"swapcase",
	"ternary",
	"title",
	"toDate",
	"toDecimal",
	"toPrettyJson",
	"toRawJson",
	"toString",
	"toStrings",
	"trim",
	"trimall",
	"trimAll",
	"trimPrefix",
	"trimSuffix",
	"trunc",
	"tuple",
	"typeIs",
	"typeIsLike",
	"typeOf",
	"uniq",
	"unixEpoch",
	"unset",
	"until",
	"untilStep",
	"untitle",
	"upper",
	"urlJoin",
	"urlParse",
	"uuidv4",
	"values",
	"without",
	"wrap",
	"wrapWith",
}

var sprigFunctions = initSprigFunctions()

var includedNames = make(map[string]int)

const includedMaxRecursion = 1000

func initSprigFunctions() template.FuncMap {
	sprigAllFunctions := sprig.TxtFuncMap()

	result := make(template.FuncMap)
	for key, value := range sprigAllFunctions {
		if slices.Contains(sprigFunctionsWhitelist, key) {
			result[key] = value
		}
	}
	return result
}

func addCustomFunctions(tmpl *template.Template) *template.FuncMap {
	functions := sprigFunctions

	functions["lookup"] = func(string, string, string, string) (map[string]interface{}, error) {
		return map[string]interface{}{}, nil
	}

	functions["include"] = func(name string, data interface{}) (string, error) {
		var buf strings.Builder
		v, ok := includedNames[name]
		if ok {
			if v > includedMaxRecursion {
				return "", errors.New("rendering t has too many recursions. Nested reference name: " + name)
			}
			includedNames[name]++
		} else {
			includedNames[name] = 1
		}
		err := tmpl.ExecuteTemplate(&buf, name, data)
		includedNames[name]--
		return buf.String(), err
	}
	functions["required"] = required

	functions["getHostByName"] = func(name string) string {
		// IP for documentation purpose
		return "192.0.2.0"
	}
	functions["fail"] = emptyText

	functions["toYaml"] = toYaml
	functions["fromYaml"] = fromYaml
	functions["fromYamlArray"] = fromYamlArray
	functions["toJson"] = toJson
	functions["fromJson"] = fromJson
	functions["fromJsonArray"] = fromJsonArray
	functions["toToml"] = toToml

	return &functions
}

func required(warningMessage string, value interface{}) (interface{}, error) {
	if value == nil {
		return nil, errors.New(warningMessage)
	}
	text, ok := value.(string)
	if ok && text == "" {
		return text, errors.New(warningMessage)
	}
	return value, nil
}

func emptyText(_ string) string {
	return ""
}

func toYaml(input interface{}) string {
	text, err := yaml.Marshal(input)
	if err == nil {
		return strings.TrimSuffix(string(text), "\n")
	}
	return ""
}

func fromYaml(input string) map[string]interface{} {
	result := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &result)
	if err != nil {
		result["Error"] = err.Error()
	}
	return result
}

func fromYamlArray(input string) []interface{} {
	result := []interface{}{}
	err := yaml.Unmarshal([]byte(input), &result)
	if err != nil {
		return []interface{}{err.Error()}
	}
	return result
}

func toJson(input interface{}) string {
	text, err := json.Marshal(input)
	if err != nil {
		return ""
	}
	return string(text)
}

func fromJson(input string) map[string]interface{} {
	result := make(map[string]interface{})
	err := json.Unmarshal([]byte(input), &result)
	if err != nil {
		result["Error"] = err.Error()
	}
	return result
}

func fromJsonArray(input string) []interface{} {
	result := []interface{}{}
	err := json.Unmarshal([]byte(input), &result)
	if err != nil {
		return []interface{}{err.Error()}
	}
	return result
}

func toToml(input interface{}) string {
	result := bytes.NewBuffer(nil)
	encoder := toml.NewEncoder(result)
	err := encoder.Encode(input)
	if err != nil {
		return err.Error()
	}
	return result.String()
}
