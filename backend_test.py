#!/usr/bin/env python3
"""
HologramX Minecraft Plugin Code Analysis Test
Tests the implementation of recent fixes for the HologramX plugin
"""

import os
import re
import sys

class HologramXTester:
    def __init__(self):
        self.base_path = "/app/src/main/java/com/hologramx"
        self.test_results = {}
        
    def read_file(self, file_path):
        """Read file content"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
            return None
    
    def test_line_spacing_implementation(self):
        """Test 1: Line Spacing Fix Implementation"""
        print("Testing Line Spacing Implementation...")
        
        hologram_file = os.path.join(self.base_path, "holograms/Hologram.java")
        command_file = os.path.join(self.base_path, "commands/HologramCommand.java")
        
        hologram_content = self.read_file(hologram_file)
        command_content = self.read_file(command_file)
        
        if not hologram_content or not command_content:
            self.test_results["line_spacing"] = False
            return False
        
        # Check if lineSpacing property exists
        has_line_spacing_property = "private double lineSpacing" in hologram_content
        
        # Check if setLineSpacing method has validation
        has_validation = "Math.max(0.1, Math.min(2.0, lineSpacing))" in hologram_content
        
        # Check if line spacing is used in spawn method
        has_usage = "currentYOffset -= lineSpacing" in hologram_content
        
        # Check if handleLineSpacing command exists
        has_command = "handleLineSpacing" in command_content and "case \"linespacing\"" in command_content
        
        success = has_line_spacing_property and has_validation and has_usage and has_command
        
        print(f"  ✓ Line spacing property: {has_line_spacing_property}")
        print(f"  ✓ Validation (0.1-2.0): {has_validation}")
        print(f"  ✓ Usage in spawn: {has_usage}")
        print(f"  ✓ Command handler: {has_command}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["line_spacing"] = success
        return success
    
    def test_visibility_distance_fix(self):
        """Test 2: Visibility Distance Fix Implementation"""
        print("\nTesting Visibility Distance Fix...")
        
        hologram_file = os.path.join(self.base_path, "holograms/Hologram.java")
        hologram_content = self.read_file(hologram_file)
        
        if not hologram_content:
            self.test_results["visibility_distance"] = False
            return False
        
        # Check if setViewRange is applied to each display entity
        has_view_range = "display.setViewRange((float) visibilityDistance)" in hologram_content
        
        # Check comment indicating it applies to full hologram
        has_comment = "apply to full hologram, not per line" in hologram_content
        
        # Check default range for unlimited
        has_default = "display.setViewRange(128.0f)" in hologram_content
        
        success = has_view_range and has_comment and has_default
        
        print(f"  ✓ setViewRange implementation: {has_view_range}")
        print(f"  ✓ Full hologram comment: {has_comment}")
        print(f"  ✓ Default range: {has_default}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["visibility_distance"] = success
        return success
    
    def test_see_through_fix(self):
        """Test 3: See-Through/XRay Fix Implementation"""
        print("\nTesting See-Through/XRay Fix...")
        
        hologram_file = os.path.join(self.base_path, "holograms/Hologram.java")
        hologram_content = self.read_file(hologram_file)
        
        if not hologram_content:
            self.test_results["see_through"] = False
            return False
        
        # Check if setSeeThrough is hardcoded to false
        has_see_through_false = "display.setSeeThrough(false)" in hologram_content
        
        # Check comment explaining xray prevention
        has_xray_comment = "prevent xray effect" in hologram_content
        
        success = has_see_through_false and has_xray_comment
        
        print(f"  ✓ setSeeThrough(false): {has_see_through_false}")
        print(f"  ✓ XRay prevention comment: {has_xray_comment}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["see_through"] = success
        return success
    
    def test_line_scale_gap_fix(self):
        """Test 4: Line Scale Gap Fix Implementation"""
        print("\nTesting Line Scale Gap Fix...")
        
        hologram_file = os.path.join(self.base_path, "holograms/Hologram.java")
        hologram_content = self.read_file(hologram_file)
        
        if not hologram_content:
            self.test_results["line_scale_gap"] = False
            return False
        
        # Check if scale validation exists (0.1-10.0 range)
        has_scale_validation = "Math.max(0.1f, Math.min(10.0f, scale" in hologram_content
        
        # Check if per-line scaling is properly implemented
        has_per_line_scaling = "getLineScaleX(lineIndex)" in hologram_content
        
        # Check if line spacing is separate from scaling
        has_separate_spacing = "currentYOffset -= lineSpacing" in hologram_content
        
        success = has_scale_validation and has_per_line_scaling and has_separate_spacing
        
        print(f"  ✓ Scale validation (0.1-10.0): {has_scale_validation}")
        print(f"  ✓ Per-line scaling: {has_per_line_scaling}")
        print(f"  ✓ Separate line spacing: {has_separate_spacing}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["line_scale_gap"] = success
        return success
    
    def test_direct_commands_removal(self):
        """Test 5: Direct Commands Removal"""
        print("\nTesting Direct Commands Removal...")
        
        command_file = os.path.join(self.base_path, "commands/HologramCommand.java")
        command_content = self.read_file(command_file)
        
        if not command_content:
            self.test_results["direct_commands"] = False
            return False
        
        # Extract the main switch statement
        switch_pattern = r'switch \(subCommand\) \{(.*?)\}'
        switch_match = re.search(switch_pattern, command_content, re.DOTALL)
        
        if not switch_match:
            print("  ✗ Could not find main switch statement")
            self.test_results["direct_commands"] = False
            return False
        
        switch_content = switch_match.group(1)
        
        # Check that direct commands are NOT in the main switch
        forbidden_commands = ["scale", "addLine", "moveHere", "setLine", "removeLine"]
        has_forbidden = False
        
        for cmd in forbidden_commands:
            if f'case "{cmd.lower()}"' in switch_content:
                print(f"  ✗ Found forbidden direct command: {cmd}")
                has_forbidden = True
        
        # Check that only edit command exists for editing
        has_only_edit = 'case "edit"' in switch_content
        has_edit_comment = "ONLY WAY TO EDIT" in command_content
        
        success = not has_forbidden and has_only_edit and has_edit_comment
        
        print(f"  ✓ No forbidden direct commands: {not has_forbidden}")
        print(f"  ✓ Edit command exists: {has_only_edit}")
        print(f"  ✓ 'ONLY WAY TO EDIT' comment: {has_edit_comment}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["direct_commands"] = success
        return success
    
    def test_unified_edit_system(self):
        """Test 6: Unified Edit System Implementation"""
        print("\nTesting Unified Edit System...")
        
        command_file = os.path.join(self.base_path, "commands/HologramCommand.java")
        command_content = self.read_file(command_file)
        
        if not command_content:
            self.test_results["unified_edit"] = False
            return False
        
        # Check if handleEdit method exists
        has_handle_edit = "private void handleEdit(CommandSender sender, String[] args)" in command_content
        
        # Check if edit commands are properly routed
        edit_commands = ["movehere", "scale", "linespacing", "visibilitydistance", "addline", "setline"]
        has_edit_routing = all(f'case "{cmd}"' in command_content for cmd in edit_commands)
        
        # Check if edit usage is shown
        has_edit_usage = "sendEditUsage(player)" in command_content
        
        # Check if edit args are properly handled
        has_args_handling = "String[] editArgs = new String[args.length - 1]" in command_content
        
        success = has_handle_edit and has_edit_routing and has_edit_usage and has_args_handling
        
        print(f"  ✓ handleEdit method: {has_handle_edit}")
        print(f"  ✓ Edit command routing: {has_edit_routing}")
        print(f"  ✓ Edit usage display: {has_edit_usage}")
        print(f"  ✓ Args handling: {has_args_handling}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["unified_edit"] = success
        return success
    
    def test_tab_completion(self):
        """Test 7: Tab Completion for Edit Commands"""
        print("\nTesting Tab Completion...")
        
        command_file = os.path.join(self.base_path, "commands/HologramCommand.java")
        command_content = self.read_file(command_file)
        
        if not command_content:
            self.test_results["tab_completion"] = False
            return False
        
        # Check if tab completion includes lineSpacing
        has_linespacing_completion = '"lineSpacing"' in command_content
        
        # Check if edit commands are in tab completion
        has_edit_completion = 'if ("edit".equals(subCommand))' in command_content
        
        success = has_linespacing_completion and has_edit_completion
        
        print(f"  ✓ lineSpacing in completion: {has_linespacing_completion}")
        print(f"  ✓ Edit command completion: {has_edit_completion}")
        print(f"  Result: {'PASS' if success else 'FAIL'}")
        
        self.test_results["tab_completion"] = success
        return success
    
    def run_all_tests(self):
        """Run all tests and return summary"""
        print("=" * 60)
        print("HologramX Plugin Implementation Analysis")
        print("=" * 60)
        
        tests = [
            self.test_line_spacing_implementation,
            self.test_visibility_distance_fix,
            self.test_see_through_fix,
            self.test_line_scale_gap_fix,
            self.test_direct_commands_removal,
            self.test_unified_edit_system,
            self.test_tab_completion
        ]
        
        passed = 0
        total = len(tests)
        
        for test in tests:
            if test():
                passed += 1
        
        print("\n" + "=" * 60)
        print(f"SUMMARY: {passed}/{total} tests passed")
        print("=" * 60)
        
        if passed == total:
            print("🎉 ALL TESTS PASSED - Implementation looks correct!")
            return True
        else:
            print("❌ Some tests failed - Implementation needs review")
            return False

if __name__ == "__main__":
    tester = HologramXTester()
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)